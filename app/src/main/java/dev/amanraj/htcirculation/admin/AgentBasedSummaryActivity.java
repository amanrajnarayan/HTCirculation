package dev.amanraj.htcirculation.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.AgentDateSummary;

public class AgentBasedSummaryActivity extends AppCompatActivity {

    private EditText etAgentCode;
    private Spinner spPublication;
    private RecyclerView rv;

    private final List<AgentDateSummary> list = new ArrayList<>();
    private AgentBasedSummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_based_summary);

        etAgentCode = findViewById(R.id.etAgentCode);
        spPublication = findViewById(R.id.spPublication);
        rv = findViewById(R.id.rvSummary);

        ArrayAdapter<CharSequence> pubAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.publications,
                        android.R.layout.simple_spinner_item
                );
        pubAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spPublication.setAdapter(pubAdapter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgentBasedSummaryAdapter(list);
        rv.setAdapter(adapter);

        findViewById(R.id.btnLoadSummary).setOnClickListener(v -> loadSummary());
    }

    private void loadSummary() {

        String agentCode =
                etAgentCode.getText().toString().trim().toUpperCase();
        String publication =
                spPublication.getSelectedItem().toString();

        if (agentCode.isEmpty()) {
            etAgentCode.setError("Required");
            return;
        }

        list.clear();
        adapter.notifyDataSetChanged();

        String currentMonth =
                LocalDate.now().toString().substring(0, 7);

        FirebaseFirestore.getInstance()
                .collection("purchase_orders")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Toast.makeText(
                                this,
                                "No orders found",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    for (var dateDoc : snapshot.getDocuments()) {

                        String date = dateDoc.getId();
                        if (!date.startsWith(currentMonth)) continue;

                        String docId = agentCode + "_" + publication;

                        dateDoc.getReference()
                                .collection("orders")
                                .document(docId)
                                .get()
                                .addOnSuccessListener(orderDoc -> {

                                    if (!orderDoc.exists()) return;

                                    Long q = orderDoc.getLong("quantity");
                                    if (q == null) return;

                                    fetchUnsoldAndAddRow(
                                            date,
                                            agentCode,
                                            publication,
                                            q.intValue()
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

                    list.add(
                            new AgentDateSummary(date, purchased, unsold)
                    );
                    adapter.notifyDataSetChanged();
                });
    }
}
