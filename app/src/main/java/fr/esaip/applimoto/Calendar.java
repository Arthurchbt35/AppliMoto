package fr.esaip.applimoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Calendar extends AppCompatActivity {

    private LinearLayout containerList;

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
                    containerList.removeAllViews();
                    try {
                        JSONArray array = new JSONArray(response.trim());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject event = array.getJSONObject(i);
                            String name = event.optString("sponsored_name", event.optString("name", "GP " + (i + 1)));
                            String date = event.optString("date_start", "").split("T")[0];
                            if (!name.toLowerCase().contains("test")) {
                                addRaceButton(name + "\n" + date);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur lecture JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    int code = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;
                    Toast.makeText(this, "Erreur réseau (" + code + ")", Toast.LENGTH_LONG).show();
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(stringRequest);
    }

    private void addRaceButton(String title) {
        Button btn = new Button(this);
        btn.setText(title);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Calendar.this, RaceDetail.class);
            intent.putExtra("RACE_NAME", title);
            startActivity(intent);
        });
        containerList.addView(btn);
    }

    public void clicBack(View v) {
        finish();
    }
}