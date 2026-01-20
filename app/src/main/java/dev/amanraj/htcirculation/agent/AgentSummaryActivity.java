package dev.amanraj.htcirculation.agent;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.admin.AgentBasedSummaryAdapter;
import dev.amanraj.htcirculation.model.AgentDateSummary;

public class AgentSummaryActivity extends AppCompatActivity {

    private Spinner spPublication;
    private RecyclerView rv;

    private final List<AgentDateSummary> list = new ArrayList<>();
    private AgentBasedSummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_summary);

        spPublication = findViewById(R.id.spPublication);
        rv = findViewById(R.id.rvSummary);

        ArrayAdapter<CharSequence> pubAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.publications,
                        android.R.layout.simple_spinner_item
                );
        pubAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPublication.setAdapter(pubAdapter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgentBasedSummaryAdapter(list);
        rv.setAdapter(adapter);

        findViewById(R.id.btnLoadSummary).setOnClickListener(v -> loadSummary());
    }

    private void loadSummary() {

        String publication = spPublication.getSelectedItem().toString();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        list.clear();
        adapter.notifyDataSetChanged();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        Toast.makeText(this, "Agent profile missing", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String agentCode = userDoc.getString("agentCode");
                    if (agentCode == null) return;

                    loadOrders(agentCode, publication);
                });
    }

    private void loadOrders(String agentCode, String publication) {

        FirebaseFirestore.getInstance()
                .collection("purchase_orders")
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (var dateDoc : snapshot.getDocuments()) {

                        String date = dateDoc.getId();
                        String docId = agentCode + "_" + publication;

                        dateDoc.getReference()
                                .collection("orders")
                                .document(docId)
                                .get()
                                .addOnSuccessListener(orderDoc -> {

                                    if (!orderDoc.exists()) return;

                                    Long qty = orderDoc.getLong("quantity");
                                    if (qty == null) return;

                                    fetchUnsoldAndAddRow(
                                            date,
                                            agentCode,
                                            publication,
                                            qty.intValue()
                                    );
                                });
                    }
                });
    }

    private void fetchUnsoldAndAddRow(
            String date,
            String agentCode,
            String publication,
            int purchased
    ) {

        String docId = agentCode + "_" + publication;

        FirebaseFirestore.getInstance()
                .collection("unsold_entries")
                .document(date)
                .collection("entries")
                .document(docId)
                .get()
                .addOnSuccessListener(unsoldDoc -> {

                    int unsold = 0;

                    if (unsoldDoc.exists()) {
                        Long u = unsoldDoc.getLong("unsoldQty");
                        if (u != null) unsold = u.intValue();
                    }

                    list.add(new AgentDateSummary(date, purchased, unsold));
                    adapter.notifyDataSetChanged();
                });
    }
}
