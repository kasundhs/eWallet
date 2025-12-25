package org.example.controller;

import org.example.dto.ApiResponse;
import org.example.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/partitions/{partitionId}/failover")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateLeaderFailure(
            @PathVariable int partitionId) {
        try {
            // Get status before failover
            Map<String, Object> statusBefore = walletService.getPartitionStatus(partitionId);
            
            // Simulate leader failure
            walletService.simulateLeaderFailure(partitionId);
            
            // Get status after failover
            Map<String, Object> statusAfter = walletService.getPartitionStatus(partitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("partitionId", partitionId);
            result.put("statusBefore", statusBefore);
            result.put("statusAfter", statusAfter);
            result.put("message", "Leader failure simulated. New leader elected automatically.");
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to simulate failover: " + e.getMessage()));
        }
    }

    @GetMapping("/partitions/{partitionId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPartitionStatus(
            @PathVariable int partitionId) {
        try {
            Map<String, Object> status = walletService.getPartitionStatus(partitionId);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/partitions/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllPartitionsStatus() {
        Map<String, Object> allStatus = walletService.getAllPartitionsStatus();
        return ResponseEntity.ok(ApiResponse.success(allStatus));
    }

    @PostMapping("/partitions/{partitionId}/replicas/{replicaIndex}/fail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateReplicaFailure(
            @PathVariable int partitionId,
            @PathVariable int replicaIndex) {
        try {
            walletService.simulateReplicaFailure(partitionId, replicaIndex);
            Map<String, Object> status = walletService.getPartitionStatus(partitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("partitionId", partitionId);
            result.put("replicaIndex", replicaIndex);
            result.put("status", status);
            result.put("message", "Replica failure simulated");
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to simulate replica failure: " + e.getMessage()));
        }
    }
}

