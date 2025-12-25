package org.example.controller;

import org.example.dto.ApiResponse;
import org.example.dto.TransferRequest;
import org.example.account.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> transferFunds(@RequestBody TransferRequest request) {
        try {

            walletService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount()
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Transfer completed successfully from account " + request.getFromAccount() +
                            " to account " + request.getToAccount() + " for amount " + request.getAmount()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Transfer unsuccessful. Please check balance or account numbers."
            ));
        }
    }
}

