package dev.amanraj.htcirculation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.admin.AdminDashboardActivity;
import dev.amanraj.htcirculation.agent.AgentDashboardActivity;


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String expectedRole; // "admin" or "agent"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btn_Login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Enable offline persistence
        FirebaseFirestoreSettings settings =
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build();
        db.setFirestoreSettings(settings);

        // Get role from intent
        expectedRole = getIntent().getStringExtra("ROLE");

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {

                                btnLogin.setEnabled(true);

                                if (!doc.exists()) {
                                    Toast.makeText(this,
                                            "User data not found",
                                            Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    return;
                                }

                                String actualRole = doc.getString("role");

                                if (actualRole == null || !actualRole.equals(expectedRole)) {
                                    Toast.makeText(this,
                                            "Unauthorized role",
                                            Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    return;
                                }

                                if ("admin".equals(actualRole)) {
                                    startActivity(new Intent(this, AdminDashboardActivity.class));
                                } else {
                                    startActivity(new Intent(this, AgentDashboardActivity.class));
                                }

                                finish();
                            })
//                            .addOnFailureListener(e -> {
//                                btnLogin.setEnabled(true);
//                                Toast.makeText(this,
//                                        "Network error. Please try again.",
//                                        Toast.LENGTH_LONG).show();
//                                auth.signOut();
//                            });
                            .addOnFailureListener(e -> {

                                btnLogin.setEnabled(true);

                                String message;

                                if (e instanceof com.google.firebase.firestore.FirebaseFirestoreException) {

                                    com.google.firebase.firestore.FirebaseFirestoreException ex =
                                            (com.google.firebase.firestore.FirebaseFirestoreException) e;

                                    switch (ex.getCode()) {

                                        case PERMISSION_DENIED:
                                            message = "Access denied by security rules.";
                                            break;

                                        case UNAVAILABLE:
                                            message = "No internet connection.";
                                            break;

                                        default:
                                            message = "Firestore error: " + ex.getCode();
                                    }

                                } else {
                                    message = "Error: " + e.getMessage();
                                }

                                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                                auth.signOut();
                            });
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this,
                            "Invalid credentials",
                            Toast.LENGTH_SHORT).show();
                });
    }
}