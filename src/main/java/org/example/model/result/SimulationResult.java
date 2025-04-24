package org.example.model.result;

import org.example.model.stats.CloudletStats;
import org.example.model.stats.DatacenterStats;
import org.example.model.stats.VmStats;

import java.util.List;

public class SimulationResult {
    private List<DatacenterStats> datacenterStats;
    private List<VmStats> vmStats;
    private List<CloudletStats> cloudletStats;
    private SimulationSummary summary;

    public SimulationResult(List<DatacenterStats> datacenterStats, List<VmStats> vmStats,
                            List<CloudletStats> cloudletStats, SimulationSummary summary) {
        this.datacenterStats = datacenterStats;
        this.vmStats = vmStats;
        this.cloudletStats = cloudletStats;
        this.summary = summary;
    }

    public List<DatacenterStats> getDatacenterStats() { return datacenterStats; }
    public List<VmStats> getVmStats() { return vmStats; }
    public List<CloudletStats> getCloudletStats() { return cloudletStats; }
    public SimulationSummary getSummary() { return summary; }
}