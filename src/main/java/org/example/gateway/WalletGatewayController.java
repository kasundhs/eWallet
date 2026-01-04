package org.example.gateway;

import org.example.account.WalletService;
import org.example.dto.CreateAccountRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gateway")
public class WalletGatewayController {

    private final PartitionGatewayService partitionGateway;
    private final WalletService walletService;

    public WalletGatewayController(
            PartitionGatewayService partitionGateway,
            WalletService walletService) {
        this.partitionGateway = partitionGateway;
        this.walletService = walletService;
    }

    @PostMapping("/accounts")
    public void createAccount(@RequestBody CreateAccountRequest request) {
        int partition = partitionGateway.resolvePartition(request.getAccountNumber());

        // Forward ONLY if this instance owns the partition
        walletService.createAccount(
                request.getAccountNumber(),
                request.getHolderName(),
                request.getNic(),
                request.getBalance()
        );
    }
}
