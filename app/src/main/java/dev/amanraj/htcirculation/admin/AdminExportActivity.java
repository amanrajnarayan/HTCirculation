package dev.amanraj.htcirculation.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.util.CsvExportUtil;

public class AdminExportActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_export);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnDailyExport)
                .setOnClickListener(v -> exportDaily());

        findViewById(R.id.btnMonthlyExport)
                .setOnClickListener(v -> exportMonthly());
    }

    //daily export

    private void exportDaily() {

        String today = LocalDate.now().toString();
        List<String[]> rows = new ArrayList<>();

        rows.add(new String[]{
                "Agent Code", "Agent Name", "District",
                "Publication", "Purchase Order"
        });

        db.collection("purchase_orders")
                .document(today)
                .collection("orders")
                .get()
                .addOnSuccessListener(qs -> {

                    if (qs.isEmpty()) {
                        Toast.makeText(this, "No orders today", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    qs.forEach(doc -> rows.add(new String[]{
                            doc.getString("agentCode"),
                            doc.getString("agentName"),
                            doc.getString("district"),
                            doc.getString("publication"),
                            String.valueOf(doc.getLong("quantity"))
                    }));

                    CsvExportUtil.export(
                            this,
                            "daily_export_" + today + ".csv",
                            rows
                    );
                });
    }

    //monthly export

    private void exportMonthly() {

        String month = LocalDate.now().toString().substring(0, 7);

        Map<String, SummaryRow> map = new HashMap<>();

        db.collection("purchase_orders")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final int[] pendingDates = {0};

                    snapshot.forEach(dateDoc -> {
                        String date = dateDoc.getId();
                        if (!date.startsWith(month)) return;

                        pendingDates[0]++;

                        dateDoc.getReference()
                                .collection("orders")
                                .get()
                                .addOnSuccessListener(qs -> {

                                    qs.forEach(doc -> {

                                        String agentCode = doc.getString("agentCode");
                                        String publication = doc.getString("publication");
                                        String key = agentCode + "_" + publication;

                                        SummaryRow row =
                                                map.computeIfAbsent(key, k ->
                                                        new SummaryRow(
                                                                agentCode,
                                                                doc.getString("agentName"),
                                                                doc.getString("district"),
                                                                publication
                                                        )
                                                );

                                        row.purchased += doc.getLong("quantity").intValue();
                                    });

                                    fetchUnsoldForDate(map, date, () -> {
                                        pendingDates[0]--;
                                        if (pendingDates[0] == 0) {
                                            exportMonthlyCsv(month, map);
                                        }
                                    });
                                });
                    });
                });
    }

    private void fetchUnsoldForDate(
            Map<String, SummaryRow> map,
            String date,
            Runnable onDone
    ) {

        db.collection("unsold_entries")
                .document(date)
                .collection("entries")
                .get()
                .addOnSuccessListener(qs -> {

                    qs.forEach(doc -> {
                        String agentCode = doc.getString("agentCode");
                        String publication = doc.getString("publication");
                        String key = agentCode + "_" + publication;

                        SummaryRow row = map.get(key);
                        if (row != null && doc.getLong("unsoldQty") != null) {
                            row.unsold += doc.getLong("unsoldQty").intValue();
                        }
                    });

                    onDone.run();
                });
    }

    private void exportMonthlyCsv(String month, Map<String, SummaryRow> map) {

        List<String[]> rows = new ArrayList<>();

        rows.add(new String[]{
                "Agent Code", "Agent Name", "District",
                "Publication", "Purchased", "Unsold", "Net Sold"
        });

        map.values().forEach(r -> rows.add(new String[]{
                r.agentCode,
                r.agentName,
                r.district,
                r.publication,
                String.valueOf(r.purchased),
                String.valueOf(r.unsold),
                String.valueOf(r.purchased - r.unsold)
        }));

        CsvExportUtil.export(
                this,
                "monthly_export_" + month + ".csv",
                rows
        );
    }

    //helper model

    static class SummaryRow {
        String agentCode, agentName, district, publication;
        int purchased = 0;
        int unsold = 0;

        SummaryRow(String a, String n, String d, String p) {
            agentCode = a;
            agentName = n;
            district = d;
            publication = p;
        }
    }
}
