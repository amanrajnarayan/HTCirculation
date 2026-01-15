package dev.amanraj.htcirculation.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.PurchaseOrder;

public class AdminPurchaseSummaryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TextView tvDate, tvTotals;

    private int totalQuantity = 0;
    private int totalAgents = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_purchase_summary);

        rvOrders = findViewById(R.id.rvOrders);
        tvDate = findViewById(R.id.tvDate);
        tvTotals = findViewById(R.id.tvTotals);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        loadTodayOrders();
    }

    private void loadTodayOrders() {

        String today = LocalDate.now().toString();
        tvDate.setText(today);

        FirebaseFirestore.getInstance()
                .collection("purchase_orders")
                .document(today)
                .collection("orders")
                .orderBy("agentCode")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<PurchaseOrder> orderList = new ArrayList<>();
                    totalQuantity = 0;
                    totalAgents = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        PurchaseOrder order = doc.toObject(PurchaseOrder.class);
                        if (order != null) {
                            orderList.add(order);
                            totalQuantity += order.quantity;
                            totalAgents++;
                        }
                    }

                    tvTotals.setText(
                            "Agents: " + totalAgents +
                                    " | Total Qty: " + totalQuantity
                    );

                    rvOrders.setAdapter(new PurchaseOrderAdapter(orderList));

                    if (orderList.isEmpty()) {
                        Toast.makeText(this,
                                "No purchase orders submitted today",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load purchase orders",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
