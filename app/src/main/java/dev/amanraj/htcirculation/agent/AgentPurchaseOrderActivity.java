package dev.amanraj.htcirculation.agent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dev.amanraj.htcirculation.R;

public class AgentPurchaseOrderActivity extends AppCompatActivity {

    private EditText etQuantity, etReturnAllowed, etIssueDate;
    private Spinner spPublication;
    private Button btnSubmit;

    private static final int ORDER_CUTOFF_HOUR = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_purchase_order);

        etQuantity = findViewById(R.id.etQuantity);
        etReturnAllowed = findViewById(R.id.etReturnAllowed);
        etIssueDate = findViewById(R.id.etIssueDate);
        spPublication = findViewById(R.id.spPublication);
        btnSubmit = findViewById(R.id.btnSubmitOrder);

        ArrayAdapter<CharSequence> publicationAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.publications,
                        android.R.layout.simple_spinner_item
                );
        publicationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spPublication.setAdapter(publicationAdapter);

        etIssueDate.setOnClickListener(v -> openDatePicker());
        btnSubmit.setOnClickListener(v -> submitClicked());

        if (isOrderCutoffPassed()) {
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.5f);
            Toast.makeText(this,
                    "Order time closed (after 4:00 PM)",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = year + "-"
                            + String.format("%02d", month + 1) + "-"
                            + String.format("%02d", day);
                    etIssueDate.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void submitClicked() {

        if (isOrderCutoffPassed()) {
            Toast.makeText(this,
                    "Order cannot be placed after 4:00 PM",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String qtyStr = etQuantity.getText().toString().trim();
        String retStr = etReturnAllowed.getText().toString().trim();
        String issueDate = etIssueDate.getText().toString().trim();
        String publication = spPublication.getSelectedItem().toString();

        if (issueDate.isEmpty()) {
            Toast.makeText(this, "Select issue date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qtyStr.isEmpty()) {
            etQuantity.setError("Required");
            return;
        }

        int quantity, returnAllowed;
        try {
            quantity = Integer.parseInt(qtyStr);
            returnAllowed = retStr.isEmpty() ? 0 : Integer.parseInt(retStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchAgentProfileAndSubmit(quantity, returnAllowed, issueDate, publication);
    }

    private void fetchAgentProfileAndSubmit(
            int quantity,
            int returnAllowed,
            String issueDate,
            String publication
    ) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this,
                                "Agent profile not found",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    submitOrder(
                            doc.getString("agentCode"),
                            doc.getString("name"),
                            doc.getString("district"),
                            quantity,
                            returnAllowed,
                            issueDate,
                            publication
                    );
                });
    }

    private void submitOrder(
            String agentCode,
            String agentName,
            String district,
            int quantity,
            int returnAllowed,
            String issueDate,
            String publication
    ) {

        String orderDate = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ FORCE-CREATE DATE DOCUMENT
        Map<String, Object> dateMeta = new HashMap<>();
        dateMeta.put("date", orderDate);
        dateMeta.put("createdAt", FieldValue.serverTimestamp());

        db.collection("purchase_orders")
                .document(orderDate)
                .set(dateMeta, SetOptions.merge());

        String orderId = agentCode + "_" + publication;

        DocumentReference orderRef = db
                .collection("purchase_orders")
                .document(orderDate)
                .collection("orders")
                .document(orderId);

        orderRef.get().addOnSuccessListener(snapshot -> {

            if (snapshot.exists()) {
                Toast.makeText(this,
                        "Order already submitted for " + publication,
                        Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("agentCode", agentCode);
            data.put("agentName", agentName);
            data.put("district", district);
            data.put("publication", publication);
            data.put("issueDate", issueDate);
            data.put("orderDate", orderDate);
            data.put("quantity", quantity);
            data.put("returnAllowed", returnAllowed);
            data.put("status", "submitted");
            data.put("submittedAt", FieldValue.serverTimestamp());

            orderRef.set(data)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this,
                                "Order submitted successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
        });
    }

    private boolean isOrderCutoffPassed() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        return hour >= ORDER_CUTOFF_HOUR;
    }
}
