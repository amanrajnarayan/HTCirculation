//package dev.amanraj.htcirculation.admin;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//import dev.amanraj.htcirculation.R;
//import dev.amanraj.htcirculation.model.AgentMonthSummary;
//
//public class MonthBasedSummaryAdapter
//        extends RecyclerView.Adapter<MonthBasedSummaryAdapter.ViewHolder> {
//
//    private List<AgentMonthSummary> list;
//
//    public MonthBasedSummaryAdapter(List<AgentMonthSummary> list) {
//        this.list = list;
//    }
//
//    public void update(List<AgentMonthSummary> newList) {
//        this.list = newList;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(
//            @NonNull ViewGroup parent,
//            int viewType
//    ) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_month_summary, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(
//            @NonNull ViewHolder holder,
//            int position
//    ) {
//        AgentMonthSummary s = list.get(position);
//
//        holder.tvAgentCode.setText(s.agentCode);
//        holder.tvPurchased.setText("Purchased: " + s.totalPurchased);
//        holder.tvUnsold.setText("Unsold: " + s.totalUnsold);
//        holder.tvNetSold.setText("Net Sold: " + s.getNetSold());
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//
//        TextView tvAgentCode, tvPurchased, tvUnsold, tvNetSold;
//
//        ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvAgentCode = itemView.findViewById(R.id.tvAgentCode);
//            tvPurchased = itemView.findViewById(R.id.tvPurchased);
//            tvUnsold = itemView.findViewById(R.id.tvUnsold);
//            tvNetSold = itemView.findViewById(R.id.tvNetSold);
//        }
//    }
//}
package dev.amanraj.htcirculation.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.AgentMonthSummary;

public class MonthBasedSummaryAdapter
        extends RecyclerView.Adapter<MonthBasedSummaryAdapter.VH> {

    private List<AgentMonthSummary> list;

    public MonthBasedSummaryAdapter(List<AgentMonthSummary> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_month_summary,
                        parent,
                        false
                );
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VH h,
            int position
    ) {

        AgentMonthSummary s = list.get(position);

        h.tvAgent.setText(s.agentCode);
        h.tvOrdered.setText("Purchased: " + s.totalPurchased);
        h.tvUnsold.setText("Unsold: " + s.totalUnsold);
        h.tvNetSold.setText(
                "Net Sold: " + (s.totalPurchased - s.totalUnsold)
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(List<AgentMonthSummary> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvAgent, tvOrdered, tvUnsold, tvNetSold;

        VH(@NonNull View itemView) {
            super(itemView);

            tvAgent = itemView.findViewById(R.id.tvAgent);
            tvOrdered = itemView.findViewById(R.id.tvOrdered);
            tvUnsold = itemView.findViewById(R.id.tvUnsold);
            tvNetSold = itemView.findViewById(R.id.tvNetSold);
        }
    }
}
