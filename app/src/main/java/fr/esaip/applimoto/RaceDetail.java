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

    private void fetchSessionsResults(String broadcastEventId) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // ÉTAPE 1 : Récupérer l'ID de la saison 2026 dans l'API des résultats
        String urlSeasons = "https://api.motogp.pulselive.com/motogp/v1/results/seasons";

        StringRequest requestSeasons = new StringRequest(Request.Method.GET, urlSeasons,
                responseSeasons -> {
                    try {
                        JSONArray seasons = new JSONArray(responseSeasons.trim());
                        String seasonUuid = "";
                        for (int i = 0; i < seasons.length(); i++) {
                            JSONObject s = seasons.getJSONObject(i);
                            if (s.optInt("year") == 2026) { // On cherche l'année en cours
                                seasonUuid = s.optString("id");
                                break;
                            }
                        }

                        if (!seasonUuid.isEmpty()) {
                            fetchResultsEventId(queue, seasonUuid, broadcastEventId);
                        } else {
                            tvRaceResults.setText("🏁 Statut : Grand Prix terminé\n❌ Saison 2026 introuvable");
                        }
                    } catch (Exception e) {
                        tvRaceResults.setText("🏁 Erreur JSON (Saisons) : " + e.getMessage());
                    }
                },
                error -> tvRaceResults.setText("🏁 Échec API (Saisons) : Code HTTP " + (error.networkResponse != null ? error.networkResponse.statusCode : "Inconnu"))) {
            @Override
            public Map<String, String> getHeaders() { return getApiHeaders(); }
        };

        // Configuration du Timeout pour Volley
        requestSeasons.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(requestSeasons);
    }

    private void fetchResultsEventId(RequestQueue queue, String seasonUuid, String broadcastEventId) {
        // ÉTAPE 2 : Trouver le bon UUID d'événement pour la Results API
        String urlEvents = "https://api.motogp.pulselive.com/motogp/v1/results/events?seasonUuid=" + seasonUuid;

        StringRequest requestEvents = new StringRequest(Request.Method.GET, urlEvents,
                responseEvents -> {
                    try {
                        JSONArray events = new JSONArray(new String(responseEvents.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim());
                        String resultsEventUuid = "";

                        for (int i = 0; i < events.length(); i++) {
                            JSONObject ev = events.getJSONObject(i);
                            // toad_api_uuid est la clé qui lie la Results API à la Broadcast API du calendrier
                            if (broadcastEventId.equals(ev.optString("toad_api_uuid"))) {
                                resultsEventUuid = ev.optString("id");
                                break;
                            }
                        }

                        if (!resultsEventUuid.isEmpty()) {
                            fetchFinalSessions(queue, resultsEventUuid);
                        } else {
                            tvRaceResults.setText("🏁 Statut : Grand Prix terminé\n❌ Correspondance d'événement introuvable (404 évité)");
                        }
                    } catch (Exception e) {
                        tvRaceResults.setText("🏁 Erreur JSON (Events) : " + e.getMessage());
                    }
                },
                error -> tvRaceResults.setText("🏁 Échec API (Events) : Code HTTP " + (error.networkResponse != null ? error.networkResponse.statusCode : "Inconnu"))) {
            @Override
            public Map<String, String> getHeaders() { return getApiHeaders(); }
        };

        requestEvents.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(requestEvents);
    }

    private void fetchFinalSessions(RequestQueue queue, String resultsEventUuid) {
        // ÉTAPE 3 : Appel final des sessions avec le UUID corrigé et la catégorie MotoGP
        String categoryMotoGP = "e8c110ad-64aa-4e8e-8a86-f2f152f6a942";
        String urlSessions = "https://api.motogp.pulselive.com/motogp/v1/results/sessions?eventUuid=" + resultsEventUuid + "&categoryUuid=" + categoryMotoGP;

        StringRequest requestSessions = new StringRequest(Request.Method.GET, urlSessions,
                responseSessions -> {
                    try {
                        String utf8Response = new String(responseSessions.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        JSONArray sessions = new JSONArray(utf8Response.trim());

                        if (sessions.length() > 0) {
                            StringBuilder sb = new StringBuilder("🏁 Programme MotoGP :\n\n");
                            for (int i = 0; i < sessions.length(); i++) {
                                JSONObject session = sessions.getJSONObject(i);
                                String type = session.optString("type", "Session");

                                // Remplacement des acronymes par le nom complet en français
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
                        tvRaceResults.setText("🏁 Erreur JSON (Sessions) : " + e.getMessage());
                    }
                },
                error -> tvRaceResults.setText("🏁 Échec API (Sessions) : Code HTTP " + (error.networkResponse != null ? error.networkResponse.statusCode : "Inconnu"))) {
            @Override
            public Map<String, String> getHeaders() { return getApiHeaders(); }
        };

        requestSessions.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(requestSessions);
    }

    // Factorisation des headers pour éviter la répétition dans les 3 méthodes
    private Map<String, String> getApiHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.put("Origin", "https://www.motogp.com");
        headers.put("Referer", "https://www.motogp.com/");
        return headers;
    }
    public void clicBack(View v) {
        finish();
    }
}