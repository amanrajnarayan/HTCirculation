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

public class AgentAdapter extends RecyclerView.Adapter<AgentAdapter.AgentViewHolder> {

    private final List<Agent> agentList;

    public AgentAdapter(List<Agent> agentList) {
        this.agentList = agentList;
    }

    @NonNull
    @Override
    public AgentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agent, parent, false);
        return new AgentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgentViewHolder holder, int position) {
        Agent agent = agentList.get(position);

        holder.tvAgentCode.setText(agent.agentCode);
        holder.tvAgentName.setText(agent.name);
        holder.tvDistrict.setText(agent.district);

        // Safe drop point handling
        if (agent.dropPoint == null || agent.dropPoint.trim().isEmpty()) {
            holder.tvDropPoint.setText("Drop: —");
        } else {
            holder.tvDropPoint.setText("Drop: " + agent.dropPoint);
        }
    }

    @Override
    public int getItemCount() {
        return agentList.size();
    }

    static class AgentViewHolder extends RecyclerView.ViewHolder {

        TextView tvAgentCode, tvAgentName, tvDistrict, tvDropPoint;

        AgentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAgentCode = itemView.findViewById(R.id.tvAgentCode);
            tvAgentName = itemView.findViewById(R.id.tvAgentName);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvDropPoint = itemView.findViewById(R.id.tvDropPoint);
        }
    }
}
