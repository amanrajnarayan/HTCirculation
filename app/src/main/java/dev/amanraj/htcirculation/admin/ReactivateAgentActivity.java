package dev.amanraj.htcirculation.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.Agent;

public class ReactivateAgentActivity extends AppCompatActivity
        implements AgentDeleteAdapter.OnAgentClickListener {

    RecyclerView recyclerView;
    List<Agent> agentList = new ArrayList<>();
    AgentDeleteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactivate_agent);

        recyclerView = findViewById(R.id.rvReactivateAgents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AgentDeleteAdapter(agentList, this);
        recyclerView.setAdapter(adapter);

        loadInactiveAgents();
    }

    private void loadInactiveAgents() {
        FirebaseFirestore.getInstance()
                .collection("agents")
                .whereEqualTo("active", false)
                .orderBy("agentCode")
                .get()
                .addOnSuccessListener(query -> {
                    agentList.clear();
                    for (DocumentSnapshot doc : query) {
                        Agent agent = doc.toObject(Agent.class);
                        agentList.add(agent);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onAgentClick(Agent agent) {
        new AlertDialog.Builder(this)
                .setTitle("Reactivate Agent")
                .setMessage("Reactivate agent " + agent.agentCode + "?")
                .setPositiveButton("Reactivate",
                        (d, w) -> reactivateAgent(agent.agentCode))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reactivateAgent(String agentCode) {
        FirebaseFirestore.getInstance()
                .collection("agents")
                .document(agentCode)
                .update("active", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Agent reactivated",
                            Toast.LENGTH_SHORT).show();
                    loadInactiveAgents();
                });
    }
}
