package org.example.service;

import org.example.model.result.SimulationResult;
import org.example.model.result.SimulationSummary;
import org.example.model.stats.CloudletStats;
import org.example.model.stats.DatacenterStats;
import org.example.model.stats.VmStats;
import org.example.simulation.CloudSimPlusSimulator;
import org.example.simulation.config.SimulationConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CloudSimService {
    private SimulationResult lastSimulationResult = null;

    public SimulationResult runSimulation(int numDatacenters, int hostsPerDatacenter, int numVms, int numCloudlets) {
        SimulationConfig config = new SimulationConfig(numDatacenters, hostsPerDatacenter, numVms, numCloudlets);
        CloudSimPlusSimulator simulator = new CloudSimPlusSimulator(config);
        simulator.run();

        lastSimulationResult = simulator.getResults();
        return lastSimulationResult;
    }

    public List<DatacenterStats> getLastSimulationDatacenterStats() {
        return lastSimulationResult != null ? lastSimulationResult.getDatacenterStats() : new ArrayList<>();
    }

    public List<CloudletStats> getLastSimulationCloudletStats() {
        return lastSimulationResult != null ? lastSimulationResult.getCloudletStats() : new ArrayList<>();
    }

    public List<VmStats> getLastSimulationVmStats() {
        return lastSimulationResult != null ? lastSimulationResult.getVmStats() : new ArrayList<>();
    }

    public SimulationSummary getLastSimulationSummary() {
        return lastSimulationResult != null ? lastSimulationResult.getSummary() : new SimulationSummary();
    }
}