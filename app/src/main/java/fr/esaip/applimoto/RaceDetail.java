package fr.esaip.applimoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RaceDetail extends AppCompatActivity {

    private TextView tvRaceName, tvRaceDate, tvRaceCountry, tvRaceTrack, tvRaceResults;
    private String passedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_detail);

        // Initialisation des vues
        tvRaceName = findViewById(R.id.tv_race_name);
        tvRaceDate = findViewById(R.id.tv_race_date);
        tvRaceCountry = findViewById(R.id.tv_race_country);
        tvRaceTrack = findViewById(R.id.tv_race_track);
        tvRaceResults = findViewById(R.id.tv_race_results);

        // Récupération des données depuis Calendar
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("RACE_NAME")) {
            String passedData = intent.getStringExtra("RACE_NAME");

            if (passedData != null) {
                String[] parts = passedData.split("\n");

                if (parts.length > 0) {
                    tvRaceName.setText(parts[0]);
                }
                if (parts.length > 1) {
                    passedDate = parts[1];
                    tvRaceDate.setText("Date : " + passedDate);
                }

                // Appel API pour récupérer les détails manquants
                fetchRaceDetails(parts[0]);
            }
        }
    }

    private void fetchRaceDetails(String raceNameTarget) {
        // On récupère tout le calendrier 2026 pour filtrer l'événement cible
        String url = "https://api.motogp.pulselive.com/motogp/v1/events?seasonYear=2026";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response.trim());
                        boolean eventFound = false;

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject event = array.getJSONObject(i);
                            String apiName = event.optString("sponsored_name", event.optString("name", ""));

                            // On compare le nom reçu de l'Intent avec celui de l'API
                            if (apiName.equals(raceNameTarget)) {
                                eventFound = true;

                                // Extraction du pays et du circuit
                                String track = "Non disponible";
                                String country = "Non disponible";

                                JSONObject circuit = event.optJSONObject("circuit");
                                if (circuit != null) {
                                    track = circuit.optString("name", track);
                                    JSONObject place = circuit.optJSONObject("place");
                                    if (place != null) {
                                        JSONObject countryObj = place.optJSONObject("country");
                                        if (countryObj != null) {
                                            country = countryObj.optString("name", country);
                                        }
                                    }
                                }

                                // Mise à jour de l'UI
                                tvRaceCountry.setText("Pays : " + country);
                                tvRaceTrack.setText("Circuit : " + track);

                                // Logique pour les résultats (passé ou futur)
                                checkResultsAvailability();
                                break; // On a trouvé la course, on arrête la boucle
                            }
                        }

                        if (!eventFound) {
                            tvRaceCountry.setText("Pays : Introuvable");
                            tvRaceTrack.setText("Circuit : Introuvable");
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur de lecture des détails", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau lors de la récupération des détails", Toast.LENGTH_SHORT).show();
                    tvRaceCountry.setText("Pays : Erreur réseau");
                    tvRaceTrack.setText("Circuit : Erreur réseau");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    private void checkResultsAvailability() {
        if (!passedDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date raceDate = sdf.parse(passedDate);
                Date today = new Date();

                if (raceDate != null && raceDate.before(today)) {
                    tvRaceResults.setText("Résultats : Disponibles (Génération du classement...)");
                    // Tu pourras ajouter ici la logique ou la vue Jetpack Compose pour les résultats
                } else {
                    tvRaceResults.setText("Résultats : Course à venir");
                }
            } catch (Exception e) {
                tvRaceResults.setText("Résultats : Statut inconnu");
            }
        }
    }

    public void clicBack(View v) {
        finish();
    }
}