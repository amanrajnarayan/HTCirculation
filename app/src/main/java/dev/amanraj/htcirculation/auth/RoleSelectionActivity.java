package dev.amanraj.htcirculation.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.amanraj.htcirculation.R;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        findViewById(R.id.adm_log_btn).setOnClickListener(view ->
                openLogin("admin"));

        findViewById(R.id.agt_log_btn).setOnClickListener(view ->
                openLogin("agent"));
    }

    private void openLogin(String role){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("ROLE",role);
        startActivity(intent);
    }
}