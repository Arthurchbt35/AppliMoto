package fr.esaip.applimoto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RaceDetail extends AppCompatActivity {

    private TextView tvRaceTitle, tvCircuitName, tvRaceDates, tvStatus, tvSessionDetails;
    private LinearLayout containerSessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_detail);

        tvRaceTitle = findViewById(R.id.tv_race_title);
        tvCircuitName = findViewById(R.id.tv_circuit_name);
        tvRaceDates = findViewById(R.id.tv_race_dates);
        tvStatus = findViewById(R.id.tv_status);
        tvSessionDetails = findViewById(R.id.tv_session_details);
        containerSessions = findViewById(R.id.container_sessions);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("RACE_TITLE");
            String circuit = intent.getStringExtra("CIRCUIT_NAME");
            String dates = intent.getStringExtra("RACE_DATES");
            String eventId = intent.getStringExtra("EVENT_ID");
            boolean isFinished = intent.getBooleanExtra("IS_FINISHED", false);

            if (title != null) tvRaceTitle.setText(title);
            if (circuit != null) tvCircuitName.setText(circuit);
            if (dates != null) tvRaceDates.setText(dates);

            if (isFinished) {
                tvStatus.setText("Grand Prix terminé");
                if (eventId != null && !eventId.isEmpty()) {
                    fetchSessionsResults(eventId);
                }
            } else {
                tvStatus.setText("Week-end à venir");
            }
        }
    }

    private void fetchSessionsResults(String broadcastEventId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlSeasons = "https://api.motogp.pulselive.com/motogp/v1/results/seasons";

        StringRequest requestSeasons = new StringRequest(Request.Method.GET, urlSeasons,
                response -> {
                    try {
                        JSONArray seasons = new JSONArray(response.trim());
                        String seasonUuid = "";
                        for (int i = 0; i < seasons.length(); i++) {
                            JSONObject s = seasons.getJSONObject(i);
                            if (s.optInt("year") == 2026) {
                                seasonUuid = s.optString("id");
                                break;
                            }
                        }
                        if (!seasonUuid.isEmpty()) fetchResultsEventId(queue, seasonUuid, broadcastEventId);
                    } catch (Exception e) { tvStatus.setText("Erreur Saisons"); }
                },
                error -> tvStatus.setText("Échec API (Saisons)")) {
            @Override public Map<String, String> getHeaders() { return getApiHeaders(); }
        };
        requestSeasons.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(requestSeasons);
    }

    private void fetchResultsEventId(RequestQueue queue, String seasonUuid, String broadcastEventId) {
        String urlEvents = "https://api.motogp.pulselive.com/motogp/v1/results/events?seasonUuid=" + seasonUuid;
        StringRequest requestEvents = new StringRequest(Request.Method.GET, urlEvents,
                response -> {
                    try {
                        JSONArray events = new JSONArray(new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim());
                        String resultsEventUuid = "";
                        for (int i = 0; i < events.length(); i++) {
                            JSONObject ev = events.getJSONObject(i);
                            if (broadcastEventId.equals(ev.optString("toad_api_uuid"))) {
                                resultsEventUuid = ev.optString("id");
                                break;
                            }
                        }
                        if (!resultsEventUuid.isEmpty()) fetchFinalSessions(queue, resultsEventUuid);
                    } catch (Exception e) { tvStatus.setText("Erreur Events"); }
                },
                error -> tvStatus.setText("Échec API (Events)")) {
            @Override public Map<String, String> getHeaders() { return getApiHeaders(); }
        };
        requestEvents.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(requestEvents);
    }

    private void fetchFinalSessions(RequestQueue queue, String resultsEventUuid) {
        String categoryMotoGP = "e8c110ad-64aa-4e8e-8a86-f2f152f6a942";
        String urlSessions = "https://api.motogp.pulselive.com/motogp/v1/results/sessions?eventUuid=" + resultsEventUuid + "&categoryUuid=" + categoryMotoGP;

        StringRequest requestSessions = new StringRequest(Request.Method.GET, urlSessions,
                response -> {
                    try {
                        String utf8Response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        JSONArray sessions = new JSONArray(utf8Response.trim());

                        containerSessions.removeAllViews();
                        tvStatus.setText("Sélectionnez une session pour voir le classement :");

                        if (sessions.length() > 0) {
                            for (int i = 0; i < sessions.length(); i++) {
                                JSONObject session = sessions.getJSONObject(i);
                                String rawType = session.optString("type", "Session");
                                String sessionUuid = session.optString("id");

                                String displayType = formatSessionName(rawType);
                                addSessionButton(displayType, sessionUuid);
                            }
                        } else {
                            tvStatus.setText("Aucune session MotoGP trouvée");
                        }
                    } catch (Exception e) { tvStatus.setText("Erreur Sessions"); }
                },
                error -> tvStatus.setText("Échec API (Sessions)")) {
            @Override public Map<String, String> getHeaders() { return getApiHeaders(); }
        };
        requestSessions.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(requestSessions);
    }

    private String formatSessionName(String type) {
        switch (type) {
            case "RAC": return "Course Principale";
            case "SPR": return "Course Sprint";
            case "Q2": return "Qualifications 2";
            case "Q1": return "Qualifications 1";
            case "PR": return "Practice (Essais)";
            case "FP1": return "Essais Libres 1";
            case "FP2": return "Essais Libres 2";
            case "WUP": return "Warm Up";
            case "P": return "Essais";
            default: return type;
        }
    }

    private void addSessionButton(String sessionName, String sessionUuid) {
        Button btn = new Button(this);
        btn.setText(sessionName);
        btn.setAllCaps(false);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor("#D32F2F")); // Rouge MotoGP

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> fetchSessionClassification(sessionUuid, sessionName));
        containerSessions.addView(btn);
    }

    // NOUVELLE MÉTHODE : Récupère le classement d'une session spécifique
    private void fetchSessionClassification(String sessionUuid, String sessionName) {
        tvSessionDetails.setText("Chargement du classement pour " + sessionName + "...");

        // CORRECTION ICI : "classification" au singulier (sans le 's' final)
        String url = "https://api.motogp.pulselive.com/motogp/v1/results/classification?sessionUuid=" + sessionUuid;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        String utf8Response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                        // L'API peut encapsuler le tableau dans un objet ou le donner directement
                        JSONArray classificationArray;
                        if (utf8Response.trim().startsWith("{")) {
                            JSONObject obj = new JSONObject(utf8Response);
                            classificationArray = obj.getJSONArray("classification");
                        } else {
                            classificationArray = new JSONArray(utf8Response.trim());
                        }

                        // Vérification si la session est encore vide (ex: Course non démarrée mais créée)
                        if (classificationArray.length() == 0) {
                            tvSessionDetails.setText("Aucun classement disponible pour " + sessionName + " (Session en attente).");
                            return;
                        }

                        StringBuilder sb = new StringBuilder("🏆 Classement - " + sessionName + "\n\n");
                        for (int i = 0; i < classificationArray.length(); i++) {
                            JSONObject item = classificationArray.getJSONObject(i);
                            int position = item.optInt("position", i + 1);
                            int points = item.optInt("points", 0);

                            JSONObject rider = item.optJSONObject("rider");
                            String riderName = (rider != null) ? rider.optString("full_name", "Pilote inconnu") : "Pilote";

                            // On cherche le temps, s'il est vide on prend l'écart (gap)
                            String timeString = item.optString("time", "");
                            if (timeString.isEmpty() || timeString.equals("null")) {
                                timeString = item.optString("gap", "");
                            }

                            sb.append(position).append(". ").append(riderName);
                            if (points > 0) sb.append("  [").append(points).append(" pts]");

                            // Affichage du chrono
                            if (!timeString.isEmpty() && !timeString.equals("null")) {
                                sb.append("\n    ⏱ ").append(timeString);
                            }
                            sb.append("\n\n");
                        }

                        tvSessionDetails.setText(sb.toString());

                    } catch (Exception e) {
                        tvSessionDetails.setText("Erreur de lecture du classement.\n" + e.getMessage());
                    }
                },
                error -> {
                    // Affichage du code HTTP précis pour le débogage (ex: 403, 404)
                    int statusCode = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;
                    tvSessionDetails.setText("Impossible de récupérer le classement.\nCode HTTP : " + statusCode);
                }) {
            @Override public Map<String, String> getHeaders() { return getApiHeaders(); }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        queue.add(request);
    }
    private Map<String, String> getApiHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.put("Origin", "https://www.motogp.com");
        headers.put("Referer", "https://www.motogp.com/");
        return headers;
    }

    public void clicBack(View v) {
        finish();
    }
}