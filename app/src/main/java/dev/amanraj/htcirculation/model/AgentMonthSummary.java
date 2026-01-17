package dev.amanraj.htcirculation.model;

public class AgentMonthSummary {

    public String agentCode;
    public int totalPurchased;
    public int totalUnsold;

    public AgentMonthSummary(String agentCode) {
        this.agentCode = agentCode;
        this.totalPurchased = 0;
        this.totalUnsold = 0;
    }

    public int getNetSold() {
        return totalPurchased - totalUnsold;
    }
}
