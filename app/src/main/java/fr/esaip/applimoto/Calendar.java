package fr.esaip.applimoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class Calendar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
    }

    public void clicRaceDetail(View v) {
        startActivity(new Intent(this, RaceDetail.class));
    }

    public void clicBack(View v) {
        finish();
    }
}