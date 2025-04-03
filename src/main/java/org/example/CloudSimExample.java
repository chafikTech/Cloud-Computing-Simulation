package org.example;

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
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CloudSimExample {
    private static final int NUMBER_OF_DATACENTERS = 3;
    private static final int HOSTS_PER_DATACENTER = 5;
    private static final int NUMBER_OF_VMS = 15;
    private static final int NUMBER_OF_CLOUDLETS = 30;

    private final CloudSimPlus simulation;
    private List<Datacenter> datacenters;
    private DatacenterBroker broker;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private final Random random;

    public static void main(String[] args) {
        new CloudSimExample();
    }

    public CloudSimExample() {
        System.out.println("Starting Advanced CloudSim Plus Simulation...");

        random = new Random(42); // Fixed seed for reproducibility
        simulation = new CloudSimPlus();

        createDatacenters();
        createBroker();
        createVms();
        createCloudlets();

        runSimulation();
        printResults();
    }

    private void createDatacenters() {
        datacenters = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_DATACENTERS; i++) {
            Datacenter dc = createDatacenter(i);
            datacenters.add(dc);
            System.out.printf("Created Datacenter %d with %d hosts%n", i, HOSTS_PER_DATACENTER);
        }
    }

    private Datacenter createDatacenter(int id) {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < HOSTS_PER_DATACENTER; i++) {
            // Create variable host configurations
            int numPes = 8 + random.nextInt(9); // 8 to 16 PEs
            int mips = 1000 + random.nextInt(2000); // 1000 to 3000 MIPS
            int ram = 16384 + (random.nextInt(8) * 8192); // 16 to 80 GB RAM
            long storage = 1000000 + (random.nextInt(10) * 500000); // 1TB to 6TB
            long bw = 10000 + (random.nextInt(5) * 10000); // 10Gbps to 50Gbps

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
        System.out.println("Created DatacenterBroker");
    }

    private void createVms() {
        vmList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            // Create VMs with different configurations
            int pes = 2 + random.nextInt(5); // 2 to 6 PEs
            int mips = 800 + random.nextInt(1600); // 800 to 2400 MIPS
            int ram = 2048 + (random.nextInt(7) * 2048); // 2GB to 16GB
            long storage = 10000 + (random.nextInt(10) * 10000); // 10GB to 100GB
            long bw = 1000 + (random.nextInt(5) * 1000); // 1Gbps to 5Gbps

            Vm vm = new VmSimple(mips, pes)
                    .setRam(ram)
                    .setBw(bw)
                    .setSize(storage)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());

            vmList.add(vm);
        }

        broker.submitVmList(vmList);
        System.out.printf("Created %d VMs%n", NUMBER_OF_VMS);
    }

    private void createCloudlets() {
        cloudletList = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_CLOUDLETS; i++) {
            // Create cloudlets with different requirements
            long length = 5000 + (random.nextInt(20) * 1000); // 5k to 25k MI
            int pes = 1 + random.nextInt(4); // 1 to 4 PEs
            long fileSize = 500 + (random.nextInt(2000)); // 500 to 2500 MB input size
            long outputSize = 300 + (random.nextInt(1000)); // 300 to 1300 MB output size

            // Different utilization models for CPU, RAM, and BW
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
        System.out.printf("Created %d Cloudlets%n", NUMBER_OF_CLOUDLETS);
    }

    private void runSimulation() {
        System.out.println("Starting simulation...");
        simulation.start();
        System.out.println("Simulation finished!");
    }

    private void printResults() {
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();

        System.out.println("\n========== SIMULATION RESULTS ==========");
        System.out.printf("Number of datacenters: %d%n", datacenters.size());
        System.out.printf("Number of hosts: %d%n", datacenters.stream().mapToInt(dc -> ((DatacenterSimple)dc).getHostList().size()).sum());
        System.out.printf("Number of VMs: %d%n", vmList.size());
        System.out.printf("Number of Cloudlets: %d%n", cloudletList.size());
        System.out.printf("Completed Cloudlets: %d%n", finishedCloudlets.size());

        System.out.println("\n========== CLOUDLET EXECUTION DETAILS ==========");
        System.out.println("Cloudlet ID | Status | VM ID | Time | Start Time | Finish Time | Data Center ID");

        // Sort cloudlets by ID
        finishedCloudlets.sort(Comparator.comparingLong(Cloudlet::getId));

        for (Cloudlet cloudlet : finishedCloudlets) {
            System.out.printf("%-11d | %-7s | %-6d | %-6.2f | %-11.2f | %-12.2f | %-14d%n",
                    cloudlet.getId(),
                    cloudlet.getStatus(),
                    cloudlet.getVm().getId(),
                    cloudlet.getActualCpuTime(),
                    cloudlet.getExecStartTime(),
                    cloudlet.getFinishTime(),
                    cloudlet.getVm().getHost().getDatacenter().getId());
        }

        // Calculate and print average metrics
        double avgExecutionTime = finishedCloudlets.stream()
                .mapToDouble(Cloudlet::getActualCpuTime)
                .average()
                .orElse(0);

        double avgWaitTime = finishedCloudlets.stream()
                .mapToDouble(c -> c.getExecStartTime() - c.getSubmissionDelay())
                .average()
                .orElse(0);

        System.out.println("\n========== PERFORMANCE METRICS ==========");
        System.out.printf("Average Execution Time: %.2f seconds%n", avgExecutionTime);
        System.out.printf("Average Wait Time: %.2f seconds%n", avgWaitTime);
    }
}