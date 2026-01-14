package dev.amanraj.htcirculation.admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.Agent;

public class ViewAgentActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_agent);

        recyclerView=findViewById(R.id.rvAgents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAgents();
    }

    private void loadAgents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Agent> agentList = new ArrayList<>();
        AgentAdapter adapter = new AgentAdapter(agentList);
        recyclerView.setAdapter(adapter);

        db.collection("agents")
                .whereEqualTo("active",true)
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

}