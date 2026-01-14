package dev.amanraj.htcirculation.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.amanraj.htcirculation.R;

public class AgentMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_menu);

        findViewById(R.id.btnNewAgent).setOnClickListener(view ->
                startActivity(new Intent(this,NewAgentActivity.class)));

        findViewById(R.id.btnViewAgent).setOnClickListener(view ->
                startActivity(new Intent(this, ViewAgentActivity.class)));

        findViewById(R.id.btnDeleteAgent).setOnClickListener(view ->
                startActivity(new Intent(this, DeleteAgentActivity.class)));

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }
}