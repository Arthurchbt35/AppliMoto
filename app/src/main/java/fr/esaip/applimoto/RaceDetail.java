package fr.esaip.applimoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RaceDetail extends AppCompatActivity {

    private TextView tvRaceTitle;
    private TextView tvCircuitName;
    private TextView tvRaceDates;
    private TextView tvRaceResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_detail);

        tvRaceTitle = findViewById(R.id.tv_race_title);
        tvCircuitName = findViewById(R.id.tv_circuit_name);
        tvRaceDates = findViewById(R.id.tv_race_dates);
        tvRaceResults = findViewById(R.id.tv_race_results);

        Intent intent = getIntent();
        if (intent != null) {
            // Récupération des clés transmises par Calendar.java
            String title = intent.getStringExtra("RACE_TITLE");
            String circuit = intent.getStringExtra("CIRCUIT_NAME");
            String dates = intent.getStringExtra("RACE_DATES");
            String eventId = intent.getStringExtra("EVENT_ID");
            boolean isFinished = intent.getBooleanExtra("IS_FINISHED", false);

            // Mise à jour de l'affichage avec les infos du GP sélectionné
            if (title != null) tvRaceTitle.setText(title);
            if (circuit != null) tvCircuitName.setText("📍 " + circuit);
            if (dates != null) tvRaceDates.setText("📅 " + dates);

            if (isFinished) {
                tvRaceResults.setText("🏁 Statut : Grand Prix terminé");
                if (eventId != null && !eventId.isEmpty()) {
                    fetchSessionsResults(eventId);
                }
            } else {
                tvRaceResults.setText("⏳ Statut : Week-end à venir");
            }
        }
    }

    private void fetchSessionsResults(String eventId) {
        // NOUVELLE URL CORRECTE :
        // On cible l'endpoint des résultats, on passe l'ID de l'événement ET l'ID officiel de la catégorie MotoGP
        String categoryMotoGP = "e8c110ad-64aa-4e8e-8a86-f2f152f6a942";
        String url = "https://api.motogp.pulselive.com/motogp/v1/results/sessions?eventUuid=" + eventId + "&categoryUuid=" + categoryMotoGP;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        String utf8Response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                        // L'API renvoie directement un tableau [...]
                        JSONArray sessions = new JSONArray(utf8Response.trim());

                        if (sessions.length() > 0) {
                            StringBuilder sb = new StringBuilder("🏁 Programme MotoGP :\n\n");

                            for (int i = 0; i < sessions.length(); i++) {
                                JSONObject session = sessions.getJSONObject(i);

                                // L'API utilise la clé "type" (ex: "RAC", "Q2")
                                String type = session.optString("type", "Session");

                                // Traduction des codes de l'API pour un meilleur affichage
                                switch (type) {
                                    case "RAC": type = "Course Principale"; break;
                                    case "SPR": type = "Course Sprint"; break;
                                    case "Q2":  type = "Qualifications 2"; break;
                                    case "Q1":  type = "Qualifications 1"; break;
                                    case "PR":  type = "Practice (Essais)"; break;
                                    case "FP1": type = "Essais Libres 1"; break;
                                    case "FP2": type = "Essais Libres 2"; break;
                                    case "WUP": type = "Warm Up"; break;
                                    case "P":   type = "Essais"; break;
                                }

                                sb.append("• ").append(type).append("\n");
                            }
                            tvRaceResults.setText(sb.toString());
                        } else {
                            tvRaceResults.setText("🏁 Statut : Grand Prix terminé\n(Aucune session MotoGP trouvée)");
                        }
                    } catch (Exception e) {
                        tvRaceResults.setText("🏁 Erreur de formatage JSON :\n" + e.getMessage());
                    }
                },
                error -> {
                    int statusCode = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;
                    tvRaceResults.setText("🏁 Statut : Grand Prix terminé\n❌ Échec de l'API : Code HTTP " + statusCode);
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                // On garde les en-têtes qui simulent un navigateur web au cas où
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                headers.put("Origin", "https://www.motogp.com");
                headers.put("Referer", "https://www.motogp.com/");
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(stringRequest);
    }
    public void clicBack(View v) {
        finish();
    }
}