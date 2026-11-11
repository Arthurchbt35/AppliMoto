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
        // Interrogation de l'API avec l'ID de l'événement choisi
        String url = "https://api.motogp.pulselive.com/motogp/v1/events/" + eventId + "/sessions";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        String utf8Response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        JSONArray sessions = new JSONArray(utf8Response.trim());

                        if (sessions.length() > 0) {
                            StringBuilder sb = new StringBuilder("🏁 Sessions du week-end :\n\n");
                            for (int i = 0; i < sessions.length(); i++) {
                                JSONObject session = sessions.getJSONObject(i);
                                String sessionName = session.optString("name", "Session " + (i + 1));
                                sb.append("• ").append(sessionName).append("\n");
                            }
                            tvRaceResults.setText(sb.toString());
                        }
                    } catch (Exception ignored) {}
                },
                error -> tvRaceResults.setText("🏁 Statut : Grand Prix terminé (Détails réseau indisponibles)")) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
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