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
            int numPes = random.nextInt(8, 17);
            int mips = random.nextInt(1000, 3000);
            int ram = random.nextInt(16384, 81920, 8192);
            long storage = random.nextLong(1000000, 6000000, 500000);
            long bw = random.nextLong(10000, 60000, 10000);

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
            int pes = random.nextInt(2, 7);
            int mips = random.nextInt(800, 2400);
            int ram = random.nextInt(2048, 16384, 2048);
            long storage = random.nextLong(10000, 110000, 10000);
            long bw = random.nextLong(1000, 6000, 1000);

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
            long length = random.nextLong(5000, 25000, 1000);
            int pes = random.nextInt(1, 5);
            long fileSize = random.nextLong(500, 2500);
            long outputSize = random.nextLong(300, 1300);

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
        List<DatacenterStats> datacenterStatsList = collectDatacenterStats();

        // VM statistics
        List<VmStats> vmStatsList = collectVmStats();

        // Cloudlet statistics
        List<CloudletStats> cloudletStatsList = collectCloudletStats();

        // Summary
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
