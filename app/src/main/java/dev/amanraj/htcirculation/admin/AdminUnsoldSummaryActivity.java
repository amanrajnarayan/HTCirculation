package dev.amanraj.htcirculation.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.UnsoldEntry;

public class AdminUnsoldSummaryActivity extends AppCompatActivity {

    private EditText etIssueDate;
    private Spinner spPublication;
    private RecyclerView recyclerView;

    private final List<UnsoldEntry> list = new ArrayList<>();
    private UnsoldSummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_unsold_summary);

        etIssueDate = findViewById(R.id.etIssueDate);
        spPublication = findViewById(R.id.spPublication);
        spPublication.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String date = etIssueDate.getText().toString().trim();
                if (!date.isEmpty()) {
                    loadSummary(date);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        recyclerView = findViewById(R.id.recyclerView);

        adapter = new UnsoldSummaryAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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

        etIssueDate.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (v, y, m, d) -> {
                    String date = y + "-"
                            + String.format("%02d", m + 1) + "-"
                            + String.format("%02d", d);
                    etIssueDate.setText(date);
                    loadSummary(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void loadSummary(String issueDate) {

        String publication = spPublication.getSelectedItem().toString();
        list.clear();
        adapter.notifyDataSetChanged();
        FirebaseFirestore.getInstance()
                .collection("unsold_entries")
                .document(issueDate)
                .collection("entries")
                .whereEqualTo("publication", publication)
                .get()
                .addOnSuccessListener(qs -> {
                    list.clear();

                    for (var doc : qs.getDocuments()) {
                        UnsoldEntry e = doc.toObject(UnsoldEntry.class);
                        if (e != null) list.add(e);
                    }

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        Toast.makeText(
                                this,
                                "No unsold entries found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
