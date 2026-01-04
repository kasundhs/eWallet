package org.example.controller;

import org.example.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/replica")
public class ReplicaController {

    @Value("${wallet.replica.partition.id:0}")
    private int partitionId;

    @Value("${wallet.replica.index:0}")
    private int replicaIndex;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("partitionId", partitionId);
        health.put("replicaIndex", replicaIndex);
        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReplicaInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("partitionId", partitionId);
        info.put("replicaIndex", replicaIndex);
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}

