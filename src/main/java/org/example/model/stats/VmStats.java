package org.example.model.stats;

public class VmStats {
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
