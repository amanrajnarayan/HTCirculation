package dev.amanraj.htcirculation.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.amanraj.htcirculation.R;
import dev.amanraj.htcirculation.model.Agent;

public class AgentDeleteAdapter extends RecyclerView.Adapter<AgentDeleteAdapter.VH> {

    interface OnAgentClickListener {
        void onAgentClick(Agent agent);
    }

    private final List<Agent> agents;
    private final OnAgentClickListener listener;

    public AgentDeleteAdapter(List<Agent> agents, OnAgentClickListener listener) {
        this.agents = agents;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agent, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Agent agent = agents.get(position);
        holder.tvCode.setText(agent.agentCode);
        holder.tvName.setText(agent.name);
        holder.tvDistrict.setText(agent.district);
        holder.tvDrop.setText("Drop: " + agent.dropPoint);

        holder.itemView.setOnClickListener(v -> listener.onAgentClick(agent));
    }

    @Override
    public int getItemCount() {
        return agents.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvDistrict, tvDrop;

        VH(View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvAgentCode);
            tvName = itemView.findViewById(R.id.tvAgentName);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvDrop = itemView.findViewById(R.id.tvDropPoint);
        }
    }
}
