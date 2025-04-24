package org.example.controller;

import org.example.model.result.SimulationResult;
import org.example.model.result.SimulationSummary;
import org.example.model.stats.CloudletStats;
import org.example.model.stats.DatacenterStats;
import org.example.model.stats.VmStats;
import org.example.service.CloudSimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cloudsim")
@CrossOrigin(origins = "*")
public class CloudSimController {
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