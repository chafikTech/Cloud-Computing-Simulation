package org.example.simulation.config;

/**
 * Configuration parameters for the CloudSim simulation.
 */
public class SimulationConfig {
    private final int numberOfDatacenters;
    private final int hostsPerDatacenter;
    private final int numberOfVms;
    private final int numberOfCloudlets;
    private final long randomSeed;

    public SimulationConfig(int numberOfDatacenters, int hostsPerDatacenter, int numberOfVms, int numberOfCloudlets) {
        this(numberOfDatacenters, hostsPerDatacenter, numberOfVms, numberOfCloudlets, 42);
    }

    public SimulationConfig(int numberOfDatacenters, int hostsPerDatacenter, int numberOfVms, int numberOfCloudlets, long randomSeed) {
        this.numberOfDatacenters = numberOfDatacenters;
        this.hostsPerDatacenter = hostsPerDatacenter;
        this.numberOfVms = numberOfVms;
        this.numberOfCloudlets = numberOfCloudlets;
        this.randomSeed = randomSeed;
    }

    public int getNumberOfDatacenters() {
        return numberOfDatacenters;
    }

    public int getHostsPerDatacenter() {
        return hostsPerDatacenter;
    }

    public int getNumberOfVms() {
        return numberOfVms;
    }

    public int getNumberOfCloudlets() {
        return numberOfCloudlets;
    }

    public long getRandomSeed() {
        return randomSeed;
    }
}