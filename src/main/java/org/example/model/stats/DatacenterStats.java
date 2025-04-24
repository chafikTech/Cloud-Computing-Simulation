package org.example.model.stats;

public class DatacenterStats {
    private int id;
    private int numHosts;
    private int totalPes;
    private long totalRam;
    private long totalStorage;
    private long totalBandwidth;

    public DatacenterStats(int id, int numHosts, int totalPes, long totalRam, long totalStorage, long totalBandwidth) {
        this.id = id;
        this.numHosts = numHosts;
        this.totalPes = totalPes;
        this.totalRam = totalRam;
        this.totalStorage = totalStorage;
        this.totalBandwidth = totalBandwidth;
    }

    // Getters
    public int getId() { return id; }
    public int getNumHosts() { return numHosts; }
    public int getTotalPes() { return totalPes; }
    public long getTotalRam() { return totalRam; }
    public long getTotalStorage() { return totalStorage; }
    public long getTotalBandwidth() { return totalBandwidth; }
}
