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
 */
@RestController
@RequestMapping("/gateway/info")
public class GatewayInfo {

    @GetMapping
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("gatewayType", "Direct Service Access");
        info.put("message", "API Gateway functionality is handled by direct service endpoints");
        info.put("accountService", "http://localhost:8080/api/accounts");
        info.put("transferService", "http://localhost:8080/api/transfers");
        info.put("note", "For production, consider using Spring Cloud Gateway, Zuul, or a dedicated API Gateway");
        return info;
    }
}

