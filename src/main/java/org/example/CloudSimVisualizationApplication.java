package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class CloudSimVisualizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudSimVisualizationApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/cloudsim")
@CrossOrigin(origins = "*")
class CloudSimController {
    private final CloudSimService cloudSimService;

    public CloudSimController(CloudSimService cloudSimService) {
        this.cloudSimService = cloudSimService;
    }

    @GetMapping("/run")
    public ResponseEntity<SimulationResult> runSimulation(
            @RequestParam(defaultValue = "3") int datacenters,
            @RequestParam(defaultValue = "5") int hostsPerDatacenter,
            @RequestParam(defaultValue = "15") int vms,
            @RequestParam(defaultValue = "30") int cloudlets) {

        SimulationResult result = cloudSimService.runSimulation(datacenters, hostsPerDatacenter, vms, cloudlets);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/datacenter-stats")
    public ResponseEntity<List<DatacenterStats>> getDatacenterStats() {
        return ResponseEntity.ok(cloudSimService.getLastSimulationDatacenterStats());
    }

    @GetMapping("/cloudlet-stats")
    public ResponseEntity<List<CloudletStats>> getCloudletStats() {
        return ResponseEntity.ok(cloudSimService.getLastSimulationCloudletStats());
    }

    @GetMapping("/vm-stats")
    public ResponseEntity<List<VmStats>> getVmStats() {
        return ResponseEntity.ok(cloudSimService.getLastSimulationVmStats());
    }

    @GetMapping("/summary")
    public ResponseEntity<SimulationSummary> getSimulationSummary() {
        return ResponseEntity.ok(cloudSimService.getLastSimulationSummary());
    }
}

@Service
class CloudSimService {
    private SimulationResult lastSimulationResult = null;

    public SimulationResult runSimulation(int numDatacenters, int hostsPerDatacenter, int numVms, int numCloudlets) {
        CloudSimPlusSimulator simulator = new CloudSimPlusSimulator(numDatacenters, hostsPerDatacenter, numVms, numCloudlets);
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

class CloudSimPlusSimulator {
    private final int numberOfDatacenters;
    private final int hostsPerDatacenter;
    private final int numberOfVms;
    private final int numberOfCloudlets;

    private final CloudSimPlus simulation;
    private List<Datacenter> datacenters;
    private DatacenterBroker broker;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private final Random random;

    private SimulationResult results;

    public CloudSimPlusSimulator(int numberOfDatacenters, int hostsPerDatacenter, int numberOfVms, int numberOfCloudlets) {
        this.numberOfDatacenters = numberOfDatacenters;
        this.hostsPerDatacenter = hostsPerDatacenter;
        this.numberOfVms = numberOfVms;
        this.numberOfCloudlets = numberOfCloudlets;

        this.random = new Random(42);
        this.simulation = new CloudSimPlus();
    }

    public void run() {
        createDatacenters();
        createBroker();
        createVms();
        createCloudlets();

        simulation.start();

        collectResults();
    }

    private void createDatacenters() {
        datacenters = new ArrayList<>();
        for (int i = 0; i < numberOfDatacenters; i++) {
            Datacenter dc = createDatacenter(i);
            datacenters.add(dc);
        }
    }

    private Datacenter createDatacenter(int id) {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < hostsPerDatacenter; i++) {
            int numPes = 8 + random.nextInt(9);
            int mips = 1000 + random.nextInt(2000);
            int ram = 16384 + (random.nextInt(8) * 8192);
            long storage = 1000000 + (random.nextInt(10) * 500000);
            long bw = 10000 + (random.nextInt(5) * 10000);

            Host host = createHost(numPes, mips, ram, storage, bw);
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicyBestFit());
    }

    private Host createHost(int numPes, int mips, int ram, long storage, long bw) {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < numPes; i++) {
            peList.add(new PeSimple(mips));
        }

        return new HostSimple(ram, bw, storage, peList)
                .setVmScheduler(new VmSchedulerTimeShared());
    }

    private void createBroker() {
        broker = new DatacenterBrokerSimple(simulation);
    }

    private void createVms() {
        vmList = new ArrayList<>();
        for (int i = 0; i < numberOfVms; i++) {
            int pes = 2 + random.nextInt(5);
            int mips = 800 + random.nextInt(1600);
            int ram = 2048 + (random.nextInt(7) * 2048);
            long storage = 10000 + (random.nextInt(10) * 10000);
            long bw = 1000 + (random.nextInt(5) * 1000);

            Vm vm = new VmSimple(mips, pes)
                    .setRam(ram)
                    .setBw(bw)
                    .setSize(storage)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());

            vmList.add(vm);
        }

        broker.submitVmList(vmList);
    }

    private void createCloudlets() {
        cloudletList = new ArrayList<>();

        for (int i = 0; i < numberOfCloudlets; i++) {
            long length = 5000 + (random.nextInt(20) * 1000);
            int pes = 1 + random.nextInt(4);
            long fileSize = 500 + (random.nextInt(2000));
            long outputSize = 300 + (random.nextInt(1000));

            UtilizationModelDynamic cpuUtilization = new UtilizationModelDynamic(0.2)
                    .setMaxResourceUtilization(0.8 + (random.nextDouble() * 0.2));

            UtilizationModelDynamic ramUtilization = new UtilizationModelDynamic(0.1)
                    .setMaxResourceUtilization(0.4 + (random.nextDouble() * 0.3));

            UtilizationModelDynamic bwUtilization = new UtilizationModelDynamic(0.05)
                    .setMaxResourceUtilization(0.3 + (random.nextDouble() * 0.2));

            Cloudlet cloudlet = new CloudletSimple(length, pes)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(cpuUtilization)
                    .setUtilizationModelRam(ramUtilization)
                    .setUtilizationModelBw(bwUtilization);

            cloudletList.add(cloudlet);
        }

        broker.submitCloudletList(cloudletList);
    }

    private void collectResults() {
        // Datacenter statistics
        List<DatacenterStats> datacenterStatsList = new ArrayList<>();
        for (Datacenter datacenter : datacenters) {
            DatacenterSimple dc = (DatacenterSimple) datacenter;
            List<Host> hosts = dc.getHostList();

            int totalPes = hosts.stream().mapToInt(h -> h.getPeList().size()).sum();
            long totalRam = hosts.stream().mapToLong(h -> h.getRam().getCapacity()).sum();
            long totalStorage = hosts.stream().mapToLong(h -> h.getStorage().getCapacity()).sum();
            long totalBw = hosts.stream().mapToLong(h -> h.getBw().getCapacity()).sum();

            DatacenterStats stats = new DatacenterStats(
                    (int) dc.getId(), hosts.size(), totalPes, totalRam, totalStorage, totalBw
            );
            datacenterStatsList.add(stats);
        }

        // VM statistics
        List<VmStats> vmStatsList = new ArrayList<>();
        for (Vm vm : vmList) {
            VmStats stats = new VmStats(
                    vm.getId(),
                    vm.getHost().getId(),
                    vm.getHost().getDatacenter().getId(),
                    (int) vm.getPesNumber(),
                    vm.getMips(),
                    vm.getRam().getCapacity(),
                    vm.getStorage().getCapacity(),
                    vm.getBw().getCapacity()
            );
            vmStatsList.add(stats);
        }

        // Cloudlet statistics
        List<CloudletStats> cloudletStatsList = new ArrayList<>();
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        for (Cloudlet cloudlet : finishedCloudlets) {
            CloudletStats stats = new CloudletStats(
                    cloudlet.getId(),
                    cloudlet.getVm().getId(),
                    cloudlet.getVm().getHost().getDatacenter().getId(),
                    (int) cloudlet.getPesNumber(),
                    cloudlet.getLength(),
                    cloudlet.getActualCpuTime(),
                    cloudlet.getExecStartTime(),
                    cloudlet.getFinishTime(),
                    cloudlet.getStatus().toString()
            );
            cloudletStatsList.add(stats);
        }

        // Summary
        double avgExecutionTime = finishedCloudlets.stream()
                .mapToDouble(Cloudlet::getActualCpuTime)
                .average()
                .orElse(0);

        double avgWaitTime = finishedCloudlets.stream()
                .mapToDouble(c -> c.getExecStartTime() - c.getSubmissionDelay())
                .average()
                .orElse(0);

        SimulationSummary summary = new SimulationSummary(
                datacenters.size(),
                datacenters.stream().mapToInt(dc -> ((DatacenterSimple)dc).getHostList().size()).sum(),
                vmList.size(),
                cloudletList.size(),
                finishedCloudlets.size(),
                avgExecutionTime,
                avgWaitTime
        );

        this.results = new SimulationResult(datacenterStatsList, vmStatsList, cloudletStatsList, summary);
    }

    public SimulationResult getResults() {
        return results;
    }
}

// DTO Classes
class SimulationResult {
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

class DatacenterStats {
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

class VmStats {
    private long id;
    private long hostId;
    private long datacenterId;
    private int pes;
    private double mips;
    private long ram;
    private long storage;
    private long bandwidth;

    public VmStats(long id, long hostId, long datacenterId, int pes, double mips, long ram, long storage, long bandwidth) {
        this.id = id;
        this.hostId = hostId;
        this.datacenterId = datacenterId;
        this.pes = pes;
        this.mips = mips;
        this.ram = ram;
        this.storage = storage;
        this.bandwidth = bandwidth;
    }

    // Getters
    public long getId() { return id; }
    public long getHostId() { return hostId; }
    public long getDatacenterId() { return datacenterId; }
    public int getPes() { return pes; }
    public double getMips() { return mips; }
    public long getRam() { return ram; }
    public long getStorage() { return storage; }
    public long getBandwidth() { return bandwidth; }
}

class CloudletStats {
    private long id;
    private long vmId;
    private long datacenterId;
    private int pes;
    private long length;
    private double executionTime;
    private double startTime;
    private double finishTime;
    private String status;

    public CloudletStats(long id, long vmId, long datacenterId, int pes, long length,
                         double executionTime, double startTime, double finishTime, String status) {
        this.id = id;
        this.vmId = vmId;
        this.datacenterId = datacenterId;
        this.pes = pes;
        this.length = length;
        this.executionTime = executionTime;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.status = status;
    }

    // Getters
    public long getId() { return id; }
    public long getVmId() { return vmId; }
    public long getDatacenterId() { return datacenterId; }
    public int getPes() { return pes; }
    public long getLength() { return length; }
    public double getExecutionTime() { return executionTime; }
    public double getStartTime() { return startTime; }
    public double getFinishTime() { return finishTime; }
    public String getStatus() { return status; }
}

class SimulationSummary {
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