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

public class DeleteAgentActivity extends AppCompatActivity
        implements AgentDeleteAdapter.OnAgentClickListener {

    RecyclerView recyclerView;
    List<Agent> agentList = new ArrayList<>();
    AgentDeleteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_agent);

        recyclerView = findViewById(R.id.rvDeleteAgents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AgentDeleteAdapter(agentList, this);
        recyclerView.setAdapter(adapter);

        loadActiveAgents();
    }

    private void loadActiveAgents() {
        FirebaseFirestore.getInstance()
                .collection("agents")
                .whereEqualTo("active", true)
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
                .setTitle("Deactivate Agent")
                .setMessage("Deactivate agent " + agent.agentCode + "?\nThis can be reversed later.")
                .setPositiveButton("Deactivate", (d, w) -> deactivateAgent(agent.agentCode))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deactivateAgent(String agentCode) {
        FirebaseFirestore.getInstance()
                .collection("agents")
                .document(agentCode)
                .update("active", false)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Agent deactivated", Toast.LENGTH_SHORT).show();
                    loadActiveAgents();
                });
    }
}
