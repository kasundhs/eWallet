package org.example.controller;

import org.example.account.Account;
import org.example.dto.AccountResponse;
import org.example.dto.ApiResponse;
import org.example.dto.CreateAccountRequest;
import org.example.account.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private WalletService walletService;
    // without Autowired, have to use private WalletService walletService = new WalletService();
    // this is not a good practice. 
    // otherwise walletService would be null and cause NullPointerException

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@RequestBody CreateAccountRequest request) {
        walletService.createAccount(
                request.getAccountNumber(),
                request.getHolderName(),
                request.getNic(),
                request.getBalance()
        );

        Account account = walletService.getAccountInfo(request.getAccountNumber());
        AccountResponse response = new AccountResponse(
                account.accountNumber,
                account.holderName,
                account.nic,
                account.getBalance()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountInfo(@PathVariable long accountNumber) {
        Account account = walletService.getAccountInfo(accountNumber);
        AccountResponse response = new AccountResponse(
                account.accountNumber,
                account.holderName,
                account.nic,
                account.getBalance()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

