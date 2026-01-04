package org.example.gateway;

import org.springframework.stereotype.Service;
import wallet.partition.PartitionResolver;

@Service
public class PartitionGatewayService {

    private final PartitionResolver resolver;

    public PartitionGatewayService() {
        // must match your config
        this.resolver = new PartitionResolver(2);
    }

    public int resolvePartition(long accountNumber) {
        return resolver.resolvePartitionId(accountNumber);
    }
}
