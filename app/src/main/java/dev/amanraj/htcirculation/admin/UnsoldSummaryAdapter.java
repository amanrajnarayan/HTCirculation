package dev.amanraj.htcirculation.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.UnsoldEntry;

public class UnsoldSummaryAdapter
        extends RecyclerView.Adapter<UnsoldSummaryAdapter.ViewHolder> {

    private final List<UnsoldEntry> list;

    public UnsoldSummaryAdapter(List<UnsoldEntry> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unsold_summary, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        UnsoldEntry e = list.get(pos);

        h.tvAgent.setText(e.agentCode);
        h.tvDistrict.setText(e.district != null ? e.district:"");
        h.tvOrdered.setText("Ordered: " + e.orderedQty);
        h.tvUnsold.setText("Unsold: " + e.unsoldQty);
        h.tvNet.setText("Net Sold: " + e.getNetSold());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAgent, tvDistrict, tvOrdered, tvUnsold, tvNet;

        ViewHolder(View v) {
            super(v);
            tvAgent = v.findViewById(R.id.tvAgent);
            tvDistrict = v.findViewById(R.id.tvDistrict);
            tvOrdered = v.findViewById(R.id.tvOrdered);
            tvUnsold = v.findViewById(R.id.tvUnsold);
            tvNet = v.findViewById(R.id.tvNetSold);
        }
    }
}
