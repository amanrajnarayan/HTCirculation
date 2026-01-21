package dev.amanraj.htcirculation.model;

public class UnsoldEntry {

    public String agentCode;

    public String district;
    public String publication;
    public String issueDate;

    public int orderedQty;
    public int returnAllowed;
    public int unsoldQty;

    public UnsoldEntry() {
    }

    public int getNetSold() {
        return orderedQty - unsoldQty;
    }
}
