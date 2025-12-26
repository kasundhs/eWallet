package org.example.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Information endpoint
 * In a production environment, you would use Spring Cloud Gateway
 * or a dedicated API Gateway service (like Zuul, Kong, etc.)
 * 
 * For this implementation, API requests go directly to the service endpoints:
 * - Account Service: http://localhost:8080/api/accounts
 * - Transfer Service: http://localhost:8080/api/transfers
 * - Admin/Failover Service: http://localhost:8080/api/admin
 */
@RestController
@RequestMapping("/gateway/info")
public class GatewayInfo {

    @GetMapping
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("gatewayType", "Direct Service Access");
        info.put("message", "API Gateway functionality is handled by direct service endpoints");
        
        // Main service endpoints
        Map<String, String> services = new HashMap<>();
        services.put("accountService", "http://localhost:8080/api/accounts");
        services.put("transferService", "http://localhost:8080/api/transfers");
        services.put("adminService", "http://localhost:8080/api/admin");
        info.put("services", services);
        
        // Admin/Failover endpoints
        Map<String, String> adminEndpoints = new HashMap<>();
        adminEndpoints.put("getAllPartitionsStatus", "GET /api/admin/partitions/status");
        adminEndpoints.put("getPartitionStatus", "GET /api/admin/partitions/{partitionId}/status");
        adminEndpoints.put("simulateLeaderFailure", "POST /api/admin/partitions/{partitionId}/failover");
        adminEndpoints.put("simulateReplicaFailure", "POST /api/admin/partitions/{partitionId}/replicas/{replicaIndex}/fail");
        info.put("adminEndpoints", adminEndpoints);
        
        // Account endpoints
        Map<String, String> accountEndpoints = new HashMap<>();
        accountEndpoints.put("createAccount", "POST /api/accounts");
        accountEndpoints.put("getAccountInfo", "GET /api/accounts/{accountNumber}");
        info.put("accountEndpoints", accountEndpoints);
        
        // Transfer endpoints
        Map<String, String> transferEndpoints = new HashMap<>();
        transferEndpoints.put("transferFunds", "POST /api/transfers");
        info.put("transferEndpoints", transferEndpoints);
        
        return info;
    }
}

