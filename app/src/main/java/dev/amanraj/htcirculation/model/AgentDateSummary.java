package dev.amanraj.htcirculation.model;

public class AgentDateSummary {

    public String date;
    public int purchased;
    public int unsold;
    public int netSold;

    public AgentDateSummary() {}

    public AgentDateSummary(String date, int purchased, int unsold) {
        this.date = date;
        this.purchased = purchased;
        this.unsold = unsold;
        this.netSold = purchased - unsold;
    }
}
