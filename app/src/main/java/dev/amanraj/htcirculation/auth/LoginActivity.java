package dev.amanraj.htcirculation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.admin.AdminDashboardActivity;
import dev.amanraj.htcirculation.agent.AgentDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String expectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        expectedRole = getIntent().getStringExtra("ROLE");

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btn_Login).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            login(email, password);
        });
    }

    private void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {
                                    Toast.makeText(this, "No role assigned", Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    return;
                                }

                                String actualRole = doc.getString("role");

                                if (!expectedRole.equals(actualRole)) {
                                    Toast.makeText(this, "Unauthorized role", Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    return;
                                }

                                if ("admin".equals(actualRole)) {
                                    startActivity(new Intent(this, AdminDashboardActivity.class));
                                } else {
                                    startActivity(new Intent(this, AgentDashboardActivity.class));
                                }
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
