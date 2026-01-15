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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.publications,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPublication.setAdapter(adapter);

        etIssueDate.setOnClickListener(v -> openDatePicker());

        btnSubmit.setOnClickListener(v -> submitUnsold());
    }

//    DateDicker

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // unsold is usually for past issue

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

//    Fetch purchase order

    private void fetchPurchaseOrder() {

        String issueDate = etIssueDate.getText().toString().trim();
        String publication = spPublication.getSelectedItem().toString();

        if (issueDate.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        Toast.makeText(this, "Agent profile not found", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String agentCode = userDoc.getString("agentCode");
                    String orderDate = issueDate; // purchase orders grouped by orderDate
                    String orderId = agentCode + "_" + publication;

                    DocumentReference orderRef = db
                            .collection("purchase_orders")
                            .document(orderDate)
                            .collection("orders")
                            .document(orderId);

                    orderRef.get().addOnSuccessListener(orderDoc -> {

                        if (!orderDoc.exists()) {
                            tvOrderedInfo.setText("No purchase order found");
                            orderedQty = -1;
                            returnAllowed = -1;
                            return;
                        }

                        Long oq = orderDoc.getLong("quantity");
                        Long ra = orderDoc.getLong("returnAllowed");

                        orderedQty = oq != null ? oq.intValue() : 0;
                        returnAllowed = ra != null ? ra.intValue() : 0;

                        tvOrderedInfo.setText(
                                "Ordered: " + orderedQty
                                        + " | Return Allowed: " + returnAllowed
                        );
                    });
                });
    }

//    submit unsold

    private void submitUnsold() {

        String unsoldStr = etUnsoldQty.getText().toString().trim();
        String issueDate = etIssueDate.getText().toString().trim();
        String publication = spPublication.getSelectedItem().toString();

        if (issueDate.isEmpty()) {
            Toast.makeText(this, "Select issue date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orderedQty < 0) {
            Toast.makeText(this, "No valid purchase order", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unsoldStr.isEmpty()) {
            etUnsoldQty.setError("Required");
            return;
        }

        int unsoldQty;
        try {
            unsoldQty = Integer.parseInt(unsoldStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unsoldQty < 0 || unsoldQty > returnAllowed) {
            Toast.makeText(
                    this,
                    "Unsold must be between 0 and " + returnAllowed,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        saveUnsold(issueDate, publication, unsoldQty);
    }

//    save unsold

    private void saveUnsold(
            String issueDate,
            String publication,
            int unsoldQty
    ) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {

                    String agentCode = userDoc.getString("agentCode");
                    String agentName = userDoc.getString("name");
                    String district = userDoc.getString("district");

                    String entryId = agentCode + "_" + publication;

                    DocumentReference unsoldRef = db
                            .collection("unsold_entries")
                            .document(issueDate)
                            .collection("entries")
                            .document(entryId);

                    unsoldRef.get().addOnSuccessListener(snapshot -> {

                        if (snapshot.exists()) {
                            Toast.makeText(
                                    this,
                                    "Unsold already booked",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("agentCode", agentCode);
                        data.put("agentName", agentName);
                        data.put("district", district);
                        data.put("publication", publication);
                        data.put("issueDate", issueDate);
                        data.put("orderedQty", orderedQty);
                        data.put("returnAllowed", returnAllowed);
                        data.put("unsoldQty", unsoldQty);
                        data.put("bookedAt", FieldValue.serverTimestamp());

                        unsoldRef.set(data)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(
                                            this,
                                            "Unsold booked successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    finish();
                                });
                    });
                });
    }
}
