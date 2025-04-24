package org.example.model.result;

public class SimulationSummary {
    private int numDatacenters;
    private int numHosts;
    private int numVms;
    private int totalCloudlets;
    private int completedCloudlets;
    private double avgExecutionTime;
    private double avgWaitTime;

    public SimulationSummary() {
        // Default constructor
    }

    public SimulationSummary(int numDatacenters, int numHosts, int numVms,
                             int totalCloudlets, int completedCloudlets,
                             double avgExecutionTime, double avgWaitTime) {
        this.numDatacenters = numDatacenters;
        this.numHosts = numHosts;
        this.numVms = numVms;
        this.totalCloudlets = totalCloudlets;
        this.completedCloudlets = completedCloudlets;
        this.avgExecutionTime = avgExecutionTime;
        this.avgWaitTime = avgWaitTime;
    }

    // Getters
    public int getNumDatacenters() { return numDatacenters; }
    public int getNumHosts() { return numHosts; }
    public int getNumVms() { return numVms; }
    public int getTotalCloudlets() { return totalCloudlets; }
    public int getCompletedCloudlets() { return completedCloudlets; }
    public double getAvgExecutionTime() { return avgExecutionTime; }
    public double getAvgWaitTime() { return avgWaitTime; }
}
