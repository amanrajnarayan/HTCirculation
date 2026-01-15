package dev.amanraj.htcirculation.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.PurchaseOrder;

public class PurchaseOrderAdapter
        extends RecyclerView.Adapter<PurchaseOrderAdapter.OrderViewHolder> {

    private final List<PurchaseOrder> orderList;

    public PurchaseOrderAdapter(List<PurchaseOrder> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchase_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        PurchaseOrder order = orderList.get(position);

        holder.tvAgentCode.setText(order.agentCode);
        holder.tvAgentName.setText(order.agentName);
        holder.tvDistrict.setText(order.district);

        // ✅ NEW: publication (safe for old orders)
        holder.tvPublication.setText(
                order.publication != null
                        ? "Publication: " + order.publication
                        : "Publication: -"
        );

        // ✅ NEW: issue date (safe for old orders)
        holder.tvIssueDate.setText(
                order.issueDate != null
                        ? "Issue Date: " + order.issueDate
                        : "Issue Date: -"
        );

        holder.tvQuantity.setText("Qty: " + order.quantity);
        holder.tvReturn.setText("Return: " + order.returnAllowed);
        holder.tvStatus.setText(order.status);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvAgentCode, tvAgentName, tvDistrict,
                tvPublication, tvIssueDate,
                tvQuantity, tvReturn, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAgentCode = itemView.findViewById(R.id.tvAgentCode);
            tvAgentName = itemView.findViewById(R.id.tvAgentName);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvPublication = itemView.findViewById(R.id.tvPublication);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvReturn = itemView.findViewById(R.id.tvReturnAllowed);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
