package org.example.simulation;

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
import org.example.model.result.SimulationResult;
import org.example.model.result.SimulationSummary;
import org.example.model.stats.CloudletStats;
import org.example.model.stats.DatacenterStats;
import org.example.model.stats.VmStats;
import org.example.simulation.config.SimulationConfig;
import org.example.util.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class CloudSimPlusSimulator {
    private final SimulationConfig config;
    private final CloudSimPlus simulation;
    private List<Datacenter> datacenters;
    private DatacenterBroker broker;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private final RandomGenerator random;

    private SimulationResult results;

    // Constants for realistic cloud resource sizes (in MB unless specified)
    private static final int MIN_HOST_CORES = 16;    // Minimum cores per host (modern servers)
    private static final int MAX_HOST_CORES = 128;   // Maximum cores (high-end servers)
    private static final int MIN_HOST_MIPS = 3000;   // Minimum MIPS per core
    private static final int MAX_HOST_MIPS = 5000;   // Maximum MIPS per core

    // RAM in MB (32GB to 1TB)
    private static final int MIN_HOST_RAM = 32 * 1024;
    private static final int MAX_HOST_RAM = 1024 * 1024;
    private static final int RAM_INCREMENT = 32 * 1024;     // 32GB increments

    // Storage in MB (1TB to 16TB)
    private static final long MIN_HOST_STORAGE = 1024L * 1024L;
    private static final long MAX_HOST_STORAGE = 16 * 1024L * 1024L;
    private static final long STORAGE_INCREMENT = 1024L * 1024L;     // 1TB increments

    // Bandwidth in Mbps (10Gbps to 100Gbps converted to Mbps)
    private static final long MIN_HOST_BW = 10 * 1000;
    private static final long MAX_HOST_BW = 100 * 1000;
    private static final long BW_INCREMENT = 10 * 1000;    // 10Gbps increments

    // VM resources
    private static final int MIN_VM_CORES = 1;
    private static final int MAX_VM_CORES = 32;
    private static final int MIN_VM_MIPS = 2000;
    private static final int MAX_VM_MIPS = 4500;

    // RAM in MB (1GB to 128GB)
    private static final int MIN_VM_RAM = 1024;
    private static final int MAX_VM_RAM = 128 * 1024;
    private static final int VM_RAM_INCREMENT = 1024;  // 1GB increments

    // Storage in MB (20GB to 2TB)
    private static final long MIN_VM_STORAGE = 20 * 1024L;
    private static final long MAX_VM_STORAGE = 2 * 1024L * 1024L;
    private static final long VM_STORAGE_INCREMENT = 20 * 1024L;

    // Bandwidth in Mbps (100Mbps to 10Gbps converted to Mbps)
    private static final long MIN_VM_BW = 100;
    private static final long MAX_VM_BW = 10 * 1000;
    private static final long VM_BW_INCREMENT = 100;

    // Cloudlet resources (representing real workloads)
    private static final long MIN_CLOUDLET_LENGTH = 50000;  // More realistic task sizes
    private static final long MAX_CLOUDLET_LENGTH = 5000000;
    private static final long CLOUDLET_LENGTH_INCREMENT = 50000;

    // Cloudlet file sizes in MB (100MB to 5GB input, 50MB to 2GB output)
    private static final long MIN_CLOUDLET_FILE_SIZE = 100;
    private static final long MAX_CLOUDLET_FILE_SIZE = 5 * 1024;
    private static final long MIN_CLOUDLET_OUTPUT_SIZE = 50;
    private static final long MAX_CLOUDLET_OUTPUT_SIZE = 2 * 1024;

    public CloudSimPlusSimulator(SimulationConfig config) {
        this.config = config;
        this.random = new RandomGenerator(config.getRandomSeed());
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
        for (int i = 0; i < config.getNumberOfDatacenters(); i++) {
            Datacenter dc = createDatacenter(i);
            datacenters.add(dc);
        }
    }

    private Datacenter createDatacenter(int id) {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < config.getHostsPerDatacenter(); i++) {
            int numPes = random.nextInt(MIN_HOST_CORES, MAX_HOST_CORES + 1);
            int mips = random.nextInt(MIN_HOST_MIPS, MAX_HOST_MIPS + 1);
            int ram = random.nextInt(MIN_HOST_RAM, MAX_HOST_RAM + 1, RAM_INCREMENT);
            long storage = random.nextLong(MIN_HOST_STORAGE, MAX_HOST_STORAGE + 1, STORAGE_INCREMENT);
            long bw = random.nextLong(MIN_HOST_BW, MAX_HOST_BW + 1, BW_INCREMENT);

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
        for (int i = 0; i < config.getNumberOfVms(); i++) {
            int pes = random.nextInt(MIN_VM_CORES, MAX_VM_CORES + 1);
            int mips = random.nextInt(MIN_VM_MIPS, MAX_VM_MIPS + 1);
            int ram = random.nextInt(MIN_VM_RAM, MAX_VM_RAM + 1, VM_RAM_INCREMENT);
            long storage = random.nextLong(MIN_VM_STORAGE, MAX_VM_STORAGE + 1, VM_STORAGE_INCREMENT);
            long bw = random.nextLong(MIN_VM_BW, MAX_VM_BW + 1, VM_BW_INCREMENT);

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

        for (int i = 0; i < config.getNumberOfCloudlets(); i++) {
            long length = random.nextLong(MIN_CLOUDLET_LENGTH, MAX_CLOUDLET_LENGTH + 1, CLOUDLET_LENGTH_INCREMENT);
            int pes = random.nextInt(1, 9);  // Up to 8 cores per task
            long fileSize = random.nextLong(MIN_CLOUDLET_FILE_SIZE, MAX_CLOUDLET_FILE_SIZE + 1);
            long outputSize = random.nextLong(MIN_CLOUDLET_OUTPUT_SIZE, MAX_CLOUDLET_OUTPUT_SIZE + 1);

            // More realistic utilization patterns
            UtilizationModelDynamic cpuUtilization = new UtilizationModelDynamic(0.3)
                    .setMaxResourceUtilization(0.6 + (random.nextDouble() * 0.4));  // 60-100% max utilization

            UtilizationModelDynamic ramUtilization = new UtilizationModelDynamic(0.2)
                    .setMaxResourceUtilization(0.5 + (random.nextDouble() * 0.5));  // 50-100% max utilization

            UtilizationModelDynamic bwUtilization = new UtilizationModelDynamic(0.1)
                    .setMaxResourceUtilization(0.4 + (random.nextDouble() * 0.4));  // 40-80% max utilization

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
        List<DatacenterStats> datacenterStatsList = collectDatacenterStats();
        List<VmStats> vmStatsList = collectVmStats();
        List<CloudletStats> cloudletStatsList = collectCloudletStats();
        SimulationSummary summary = createSimulationSummary();

        this.results = new SimulationResult(datacenterStatsList, vmStatsList, cloudletStatsList, summary);
    }

    private List<DatacenterStats> collectDatacenterStats() {
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
        return datacenterStatsList;
    }

    private List<VmStats> collectVmStats() {
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
        return vmStatsList;
    }

    private List<CloudletStats> collectCloudletStats() {
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
        return cloudletStatsList;
    }

    private SimulationSummary createSimulationSummary() {
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();

        double avgExecutionTime = finishedCloudlets.stream()
                .mapToDouble(Cloudlet::getActualCpuTime)
                .average()
                .orElse(0);

        double avgWaitTime = finishedCloudlets.stream()
                .mapToDouble(c -> c.getExecStartTime() - c.getSubmissionDelay())
                .average()
                .orElse(0);

        return new SimulationSummary(
                datacenters.size(),
                datacenters.stream().mapToInt(dc -> ((DatacenterSimple)dc).getHostList().size()).sum(),
                vmList.size(),
                cloudletList.size(),
                finishedCloudlets.size(),
                avgExecutionTime,
                avgWaitTime
        );
    }

    public SimulationResult getResults() {
        return results;
    }
}