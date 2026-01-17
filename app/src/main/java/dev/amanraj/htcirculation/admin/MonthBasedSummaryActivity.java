package dev.amanraj.htcirculation.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.AgentMonthSummary;

public class MonthBasedSummaryActivity extends AppCompatActivity {

    private EditText etMonth;
    private Spinner spPublication;
    private RecyclerView rv;

    private final Map<String, AgentMonthSummary> map = new HashMap<>();
    private MonthBasedSummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_based_summary);

        etMonth = findViewById(R.id.etMonth);
        spPublication = findViewById(R.id.spPublication);
        rv = findViewById(R.id.rvSummary);

        ArrayAdapter<CharSequence> adapterPub =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.publications,
                        android.R.layout.simple_spinner_item
                );
        adapterPub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPublication.setAdapter(adapterPub);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MonthBasedSummaryAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        etMonth.setOnClickListener(v -> openMonthPicker());

        findViewById(R.id.btnLoad).setOnClickListener(v -> loadSummary());
    }

    private void openMonthPicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String m = year + "-" + String.format("%02d", month + 1);
                    etMonth.setText(m);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                1
        ).show();
    }

    private void loadSummary() {

        String month = etMonth.getText().toString().trim();
        String publication = spPublication.getSelectedItem().toString();

        if (month.isEmpty()) {
            Toast.makeText(this, "Select month", Toast.LENGTH_SHORT).show();
            return;
        }

        map.clear();
        adapter.update(new ArrayList<>());

        FirebaseFirestore.getInstance()
                .collection("purchase_orders")
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (var dateDoc : snapshot.getDocuments()) {

                        String date = dateDoc.getId();
                        if (!date.startsWith(month)) continue;

                        dateDoc.getReference()
                                .collection("orders")
                                .whereEqualTo("publication", publication)
                                .get()
                                .addOnSuccessListener(qs -> {

                                    for (var doc : qs.getDocuments()) {

                                        String agentCode =
                                                doc.getString("agentCode");
                                        int qty =
                                                doc.getLong("quantity").intValue();

                                        AgentMonthSummary s =
                                                map.computeIfAbsent(
                                                        agentCode,
                                                        AgentMonthSummary::new
                                                );

                                        s.totalPurchased += qty;

                                        fetchUnsold(
                                                date,
                                                agentCode,
                                                publication
                                        );
                                    }
                                });
                    }
                });
    }

    private void fetchUnsold(
            String date,
            String agentCode,
            String publication
    ) {

        String docId = agentCode + "_" + publication;

        FirebaseFirestore.getInstance()
                .collection("unsold_entries")
                .document(date)
                .collection("entries")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        Long u = doc.getLong("unsoldQty");
                        if (u != null) {
                            map.get(agentCode).totalUnsold += u.intValue();
                        }
                    }

                    adapter.update(new ArrayList<>(map.values()));
                });
    }
}
