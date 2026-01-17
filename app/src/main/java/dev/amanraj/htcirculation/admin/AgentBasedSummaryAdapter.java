package dev.amanraj.htcirculation.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.AgentDateSummary;

public class AgentBasedSummaryAdapter
        extends RecyclerView.Adapter<AgentBasedSummaryAdapter.VH> {

    private final List<AgentDateSummary> list;

    public AgentBasedSummaryAdapter(List<AgentDateSummary> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agent_date_summary, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AgentDateSummary s = list.get(pos);

        h.tvDate.setText("Date: " + s.date);
        h.tvPurchased.setText("Purchased: " + s.purchased);
        h.tvUnsold.setText("Unsold: " + s.unsold);
        h.tvNetSold.setText("Net Sold: " + s.netSold);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvDate, tvPurchased, tvUnsold, tvNetSold;

        VH(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
            tvPurchased = v.findViewById(R.id.tvPurchased);
            tvUnsold = v.findViewById(R.id.tvUnsold);
            tvNetSold = v.findViewById(R.id.tvNetSold);
        }
    }
}
