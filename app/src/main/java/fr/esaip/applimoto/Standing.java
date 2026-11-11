package fr.esaip.applimoto;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Standing extends AppCompatActivity {

    private LinearLayout containerStanding;
    private TextView tvHeaderName;
    private Button btnPilotes, btnEquipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standing);

        containerStanding = findViewById(R.id.container_standing);
        tvHeaderName = findViewById(R.id.tv_header_name);
        btnPilotes = findViewById(R.id.btn_pilotes);
        btnEquipes = findViewById(R.id.btn_equipes);

        afficherClassementPilotes();
    }

    private void afficherClassementPilotes() {
        containerStanding.removeAllViews();
        tvHeaderName.setText("PILOTE");
        btnPilotes.setBackgroundColor(Color.parseColor("#E10600"));
        btnEquipes.setBackgroundColor(Color.parseColor("#333333"));

        addStandingRow(1, "Marc Márquez", "Ducati Lenovo Team • 8 podiums", 7, 431);
        addStandingRow(2, "Jorge Martín", "Aprilia Racing • 9 podiums", 1, 371);
        addStandingRow(3, "Marco Bezzecchi", "Aprilia Racing • 12 podiums", 6, 357);
        addStandingRow(4, "Pedro Acosta", "Red Bull KTM Factory • 7 podiums", 1, 309);
        addStandingRow(5, "Ai Ogura", "Trackhouse MotoGP • 4 podiums", 1, 294);
        addStandingRow(6, "Fabio Di Giannantonio", "Pertamina Enduro VR46 • 3 podiums", 1, 282);
        addStandingRow(7, "Raúl Fernández", "Trackhouse MotoGP • 6 podiums", 1, 258);
        addStandingRow(8, "Álex Márquez", "Gresini Racing MotoGP • 3 podiums", 1, 197);
        addStandingRow(9, "Francesco Bagnaia", "Ducati Lenovo Team • 4 podiums", 0, 173);
        addStandingRow(10, "Enea Bastianini", "Red Bull KTM Tech3", 0, 147);
        addStandingRow(11, "Fermín Aldeguer", "Gresini Racing MotoGP • 1 podium", 0, 129);
        addStandingRow(12, "Brad Binder", "Red Bull KTM Factory", 0, 118);
        addStandingRow(13, "Luca Marini", "Honda HRC Castrol", 0, 117);
        addStandingRow(14, "Fabio Quartararo", "Monster Energy Yamaha", 0, 79);
        addStandingRow(15, "Diogo Moreira", "LCR Honda", 0, 78);
        addStandingRow(16, "Franco Morbidelli", "Pertamina Enduro VR46", 0, 70);
        addStandingRow(17, "Johann Zarco", "Castrol Honda LCR", 0, 63);
        addStandingRow(18, "Joan Mir", "Honda HRC Castrol", 0, 33);
        addStandingRow(19, "Jack Miller", "Prima Pramac Yamaha", 0, 31);
        addStandingRow(20, "Álex Rins", "Monster Energy Yamaha", 0, 29);
    }

    private void afficherClassementEquipes() {
        containerStanding.removeAllViews();
        tvHeaderName.setText("ÉQUIPE");
        btnEquipes.setBackgroundColor(Color.parseColor("#E10600"));
        btnPilotes.setBackgroundColor(Color.parseColor("#333333"));

        addStandingRow(1, "Aprilia Racing", "21 podiums cumulés", 7, 728);
        addStandingRow(2, "Ducati Lenovo Team", "12 podiums cumulés", 7, 604);
        addStandingRow(3, "Trackhouse MotoGP Team", "10 podiums cumulés", 2, 552);
        addStandingRow(4, "Red Bull KTM Factory Racing", "7 podiums cumulés", 1, 427);
        addStandingRow(5, "Pertamina Enduro VR46", "3 podiums cumulés", 1, 352);
        addStandingRow(6, "BK8 Gresini Racing MotoGP", "4 podiums cumulés", 1, 344);
        addStandingRow(7, "Red Bull KTM Tech3", "Équipe satellite KTM", 0, 169);
        addStandingRow(8, "Honda HRC Castrol", "Équipe officielle Honda", 0, 158);
        addStandingRow(9, "Honda LCR Team", "Équipe satellite Honda", 0, 141);
        addStandingRow(10, "Monster Energy Yamaha", "Équipe officielle Yamaha", 0, 108);
        addStandingRow(11, "Prima Pramac Yamaha", "Équipe partenaire Yamaha", 0, 49);
    }

    private void addStandingRow(int pos, String name, String subtitle, int wins, int points) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(24, 24, 24, 24);

        if (pos <= 3) {
            row.setBackgroundColor(Color.parseColor("#263238"));
        } else {
            row.setBackgroundColor((pos % 2 == 0) ? Color.parseColor("#1A1A1A") : Color.parseColor("#222222"));
        }

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 12);
        row.setLayoutParams(rowParams);

        // 1. Colonne Position
        TextView tvPos = new TextView(this);
        tvPos.setLayoutParams(new LinearLayout.LayoutParams(110, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPos.setTextSize(16f);
        tvPos.setTypeface(null, Typeface.BOLD);

        if (pos == 1) {
            tvPos.setText("🥇 1");
            tvPos.setTextColor(Color.parseColor("#FFD700"));
        } else if (pos == 2) {
            tvPos.setText("🥈 2");
            tvPos.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (pos == 3) {
            tvPos.setText("🥉 3");
            tvPos.setTextColor(Color.parseColor("#CD7F32"));
        } else {
            tvPos.setText("#" + pos);
            tvPos.setTextColor(Color.WHITE);
        }

        // 2. Colonne Nom + Sous-titre
        LinearLayout nameCol = new LinearLayout(this);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(15f);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(Color.parseColor("#AAAAAA"));
        tvSub.setTextSize(12f);

        nameCol.addView(tvName);
        nameCol.addView(tvSub);

        // 3. Colonne Victoires
        TextView tvWins = new TextView(this);
        tvWins.setLayoutParams(new LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvWins.setGravity(Gravity.CENTER);
        tvWins.setText(wins > 0 ? wins + " 🏆" : "-");
        tvWins.setTextColor(Color.parseColor("#DDDDDD"));
        tvWins.setTextSize(14f);

        // 4. Colonne Points
        TextView tvPts = new TextView(this);
        tvPts.setLayoutParams(new LinearLayout.LayoutParams(170, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPts.setGravity(Gravity.END);
        tvPts.setText(points + " pts");
        tvPts.setTextColor(Color.parseColor("#FF5252"));
        tvPts.setTextSize(15f);
        tvPts.setTypeface(null, Typeface.BOLD);

        row.addView(tvPos);
        row.addView(nameCol);
        row.addView(tvWins);
        row.addView(tvPts);

        containerStanding.addView(row);
    }

    public void clicPilotes(View v) {
        afficherClassementPilotes();
    }

    public void clicEquipes(View v) {
        afficherClassementEquipes();
    }

    public void clicBack(View v) {
        finish();
    }
}