package org.example.model.stats;

public class CloudletStats {
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
