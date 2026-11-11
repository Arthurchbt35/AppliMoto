package fr.esaip.applimoto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Calendar extends AppCompatActivity {

    private LinearLayout containerList;
    private JSONArray allEventsArray = new JSONArray();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        containerList = findViewById(R.id.container_list);
        fetchMotoGpCalendar();
    }

    private void fetchMotoGpCalendar() {
        String url = "https://api.motogp.pulselive.com/motogp/v1/events?seasonYear=2026";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        String utf8Response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        allEventsArray = new JSONArray(utf8Response.trim());
                        displayFilteredRaces();
                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur de lecture JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show());

        queue.add(stringRequest);
    }

    private void displayFilteredRaces() {
        containerList.removeAllViews();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int gpNumber = 1;

        for (int i = 0; i < allEventsArray.length(); i++) {
            try {
                JSONObject event = allEventsArray.getJSONObject(i);
                String name = event.optString("sponsored_name", event.optString("name", ""));

                if (event.optBoolean("test", false) || !name.toUpperCase().contains("GRAND PRIX")) {
                    continue;
                }

                String dateStart = event.optString("date_start", "").split("T")[0];
                String dateEnd = event.optString("date_end", "").split("T")[0];
                boolean isFinished = dateEnd.compareTo(today) < 0;

                if (currentFilter.equals("FINISHED") && !isFinished) continue;
                if (currentFilter.equals("UPCOMING") && isFinished) continue;

                JSONObject circuitObj = event.optJSONObject("circuit");
                String circuitName = (circuitObj != null) ? circuitObj.optString("name", "Circuit MotoGP") : "Circuit MotoGP";

                JSONObject countryObj = event.optJSONObject("country");
                String countryIso = (countryObj != null) ? countryObj.optString("iso", "") : event.optString("country", "");
                if (countryIso.length() != 2 && circuitObj != null) {
                    countryIso = circuitObj.optString("country", "");
                }

                String flag = getFlagEmoji(countryIso);
                String countryName = (countryIso.length() == 2) ? new Locale("", countryIso).getDisplayCountry(Locale.FRENCH) : "";

                String locationLine = countryName.isEmpty() ? circuitName : circuitName + " (" + countryName + ")";
                String buttonLabel = "GP #" + gpNumber + " - " + flag + " " + name + "\n"
                        + "Localisation : " + locationLine;

                addRaceButton(buttonLabel, event.optString("id", ""), flag + " " + name, locationLine, dateStart + " au " + dateEnd, isFinished);
                gpNumber++;

            } catch (Exception ignored) {}
        }
    }

    private void addRaceButton(String label, String eventId, String title, String circuit, String dates, boolean isFinished) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setAllCaps(false);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(isFinished ? Color.parseColor("#263238") : Color.parseColor("#3E2723"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        btn.setLayoutParams(params);
        btn.setPadding(24, 24, 24, 24);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Calendar.this, RaceDetail.class);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("RACE_TITLE", title);
            intent.putExtra("CIRCUIT_NAME", circuit);
            intent.putExtra("RACE_DATES", dates);
            intent.putExtra("IS_FINISHED", isFinished);
            startActivity(intent);
        });

        containerList.addView(btn);
    }

    private String getFlagEmoji(String isoCode) {
        if (isoCode == null || isoCode.length() != 2) return "🏁";
        String upper = isoCode.toUpperCase();
        int firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6;
        int secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    public void clicFilterAll(View v) {
        currentFilter = "ALL";
        displayFilteredRaces();
    }

    public void clicFilterFinished(View v) {
        currentFilter = "FINISHED";
        displayFilteredRaces();
    }

    public void clicFilterUpcoming(View v) {
        currentFilter = "UPCOMING";
        displayFilteredRaces();
    }

    public void clicBack(View v) {
        finish();
    }
}