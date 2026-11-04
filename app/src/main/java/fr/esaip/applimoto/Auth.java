package fr.esaip.applimoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Auth extends AppCompatActivity {
    EditText login, pwd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        login = findViewById(R.id.login);
        pwd = findViewById(R.id.mdp);
    }

    public void clicCloseButton(View v) {
        finishAffinity();
    }

    public void clicValidationButton(View v) {
        String email = login.getText().toString().trim();
        String password = pwd.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Authentification réussie", Toast.LENGTH_SHORT).show();
                        Intent intention = new Intent(Auth.this, MainActivity.class);
                        startActivity(intention);
                        finish();
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        Toast.makeText(this, "Compte créé et connecté !", Toast.LENGTH_SHORT).show();
                                        Intent intention = new Intent(Auth.this, MainActivity.class);
                                        startActivity(intention);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Échec : " + createTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }
}