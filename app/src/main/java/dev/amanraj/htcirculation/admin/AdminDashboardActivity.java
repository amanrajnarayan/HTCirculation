package dev.amanraj.htcirculation.admin;

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
import dev.amanraj.htcirculation.auth.RoleSelectionActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        findViewById(R.id.btnAgent).setOnClickListener(view ->
                startActivity(new Intent(this, AgentMenuActivity.class)));

        findViewById(R.id.btnPurchaseOrder).setOnClickListener(view ->
                startActivity(new Intent(this, AdminPurchaseSummaryActivity.class)));


        findViewById(R.id.btnBookUnsold).setOnClickListener(view ->
                Toast.makeText(this, "Book Unsold - Coming Soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnSummary).setOnClickListener(view ->
                Toast.makeText(this, "Summary - Coming Soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnExport).setOnClickListener(view ->
                Toast.makeText(this, "Export - Coming Soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnExit).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }
}