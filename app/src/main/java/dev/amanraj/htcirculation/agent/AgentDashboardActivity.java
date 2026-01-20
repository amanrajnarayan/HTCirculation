package dev.amanraj.htcirculation.agent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import dev.amanraj.htcirculation.R;

public class AgentDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_dashboard);

        findViewById(R.id.btnPurchaseOrder).setOnClickListener(v ->
                startActivity(new Intent(this, AgentPurchaseOrderActivity.class))
        );

        findViewById(R.id.btnBookUnsold).setOnClickListener(view ->
                startActivity(new Intent(this, AgentBookUnsoldActivity.class)));

        findViewById(R.id.btnSummary).setOnClickListener(view ->
                startActivity(new Intent(this, AgentSummaryActivity.class)));

        findViewById(R.id.btnExit).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });

    }
}