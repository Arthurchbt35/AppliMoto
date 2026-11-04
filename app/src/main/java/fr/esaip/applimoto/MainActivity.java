package fr.esaip.applimoto;;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clicCalendar(View v) {
        startActivity(new Intent(this, CalendarActivity.class));
    }

    public void clicStandings(View v) {
        startActivity(new Intent(this, StandingsActivity.class));
    }

    public void clicCompare(View v) {
        startActivity(new Intent(this, CompareActivity.class));
    }
}