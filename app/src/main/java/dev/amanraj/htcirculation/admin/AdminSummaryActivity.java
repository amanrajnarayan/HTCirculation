package dev.amanraj.htcirculation.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.amanraj.htcirculation.R;

public class AdminSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_summary);

        findViewById(R.id.btnAgentSummary).setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        AgentBasedSummaryActivity.class
                ))
        );

        findViewById(R.id.btnMonthSummary).setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        MonthBasedSummaryActivity.class
                ))
        );
    }
}
