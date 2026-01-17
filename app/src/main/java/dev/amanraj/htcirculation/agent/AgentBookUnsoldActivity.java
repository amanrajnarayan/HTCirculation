package dev.amanraj.htcirculation.agent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dev.amanraj.htcirculation.R;

public class AgentBookUnsoldActivity extends AppCompatActivity {

    private EditText etIssueDate, etUnsoldQty;
    private Spinner spPublication;
    private TextView tvOrderedInfo;
    private Button btnSubmit;

    private int orderedQty = -1;
    private int returnAllowed = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_unsold);

        etIssueDate = findViewById(R.id.etIssueDate);
        etUnsoldQty = findViewById(R.id.etUnsoldQty);
        spPublication = findViewById(R.id.spPublication);
        tvOrderedInfo = findViewById(R.id.tvOrderedInfo);
        btnSubmit = findViewById(R.id.btnSubmitUnsold);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.publications,
                        android.R.layout.simple_spinner_item
                );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spPublication.setAdapter(adapter);

        etIssueDate.setOnClickListener(v -> openDatePicker());
        btnSubmit.setOnClickListener(v -> submitUnsold());
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = year + "-"
                            + String.format("%02d", month + 1) + "-"
                            + String.format("%02d", day);
                    etIssueDate.setText(date);
                    fetchPurchaseOrder();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void fetchPurchaseOrder() {

        String issueDate = etIssueDate.getText().toString().trim();
        String publication = spPublication.getSelectedItem().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {

                    String agentCode = userDoc.getString("agentCode");
                    String orderId = agentCode + "_" + publication;

                    FirebaseFirestore.getInstance()
                            .collection("purchase_orders")
                            .document(issueDate)
                            .collection("orders")
                            .document(orderId)
                            .get()
                            .addOnSuccessListener(orderDoc -> {

                                if (!orderDoc.exists()) {
                                    tvOrderedInfo.setText("No purchase order found");
                                    orderedQty = -1;
                                    returnAllowed = -1;
                                    return;
                                }

                                orderedQty =
                                        orderDoc.getLong("quantity").intValue();
                                returnAllowed =
                                        orderDoc.getLong("returnAllowed").intValue();

                                tvOrderedInfo.setText(
                                        "Ordered: " + orderedQty +
                                                " | Return Allowed: " + returnAllowed
                                );
                            });
                });
    }

    private void submitUnsold() {

        String unsoldStr = etUnsoldQty.getText().toString().trim();
        String issueDate = etIssueDate.getText().toString().trim();
        String publication = spPublication.getSelectedItem().toString();

        if (issueDate.isEmpty() || orderedQty < 0) {
            Toast.makeText(this,
                    "Invalid order selection",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int unsoldQty = Integer.parseInt(unsoldStr);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ FORCE-CREATE DATE DOCUMENT
        Map<String, Object> dateMeta = new HashMap<>();
        dateMeta.put("date", issueDate);
        dateMeta.put("createdAt", FieldValue.serverTimestamp());

        db.collection("unsold_entries")
                .document(issueDate)
                .set(dateMeta, SetOptions.merge());

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {

                    String agentCode = userDoc.getString("agentCode");
                    String entryId = agentCode + "_" + publication;

                    DocumentReference ref = db
                            .collection("unsold_entries")
                            .document(issueDate)
                            .collection("entries")
                            .document(entryId);

                    Map<String, Object> data = new HashMap<>();
                    data.put("agentCode", agentCode);
                    data.put("publication", publication);
                    data.put("issueDate", issueDate);
                    data.put("orderedQty", orderedQty);
                    data.put("returnAllowed", returnAllowed);
                    data.put("unsoldQty", unsoldQty);
                    data.put("bookedAt", FieldValue.serverTimestamp());

                    ref.set(data)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this,
                                        "Unsold booked successfully",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }
}
