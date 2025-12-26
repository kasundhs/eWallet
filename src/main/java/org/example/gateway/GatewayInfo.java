package org.example.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * There are 2 apis use in this implementation, one for account handling and one for transfers.
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

