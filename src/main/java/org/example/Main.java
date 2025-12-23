package org.example;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {

        WalletService walletService = new WalletService(2);

        // ===============================
        // Create accounts
        // ===============================
        walletService.createAccount(
                1001L,
                "Menuka Kasun",
                "199812345678",
                50000.00
        );

        walletService.createAccount(
                1002L,
                "Nipun Perera",
                "199756789012",
                30000.00
        );

        // ===============================
        // Balance inquiry
        // ===============================
        Account acc1 = walletService.getAccountInfo(1001L);
        Account acc2 = walletService.getAccountInfo(1002L);

        LinkedList<Account> acc = new LinkedList<>();
        acc.add(acc1);
        acc.add(acc2);
        for(Account account : acc){
            System.out.println("Account Number : " + account.accountNumber);
            System.out.println("Holder Name    : " + account.holderName);
            System.out.println("NIC            : " + account.nic);
            System.out.println("Balance        : " + account.getBalance());
        }

        // ===============================
        // SAME-PARTITION TRANSFER
        // ===============================
        System.out.println("\n--- Cross partition transfer ---");
        walletService.transfer(1001L, 1002L, 5000);

        System.out.println("1001 Balance: " +
                walletService.getAccountInfo(1001L).getBalance());
        System.out.println("1002 Balance: " +
                walletService.getAccountInfo(1002L).getBalance());

        // ===============================
        // CROSS-PARTITION TRANSFER
        // ===============================
        walletService.createAccount(
                1003L,
                "Nimal Silva",
                "199945612345",
                20000.00
        );

        System.out.println("\n--- Same partition transfer ---");
        walletService.transfer(1001L, 1003L, 3000);

        System.out.println("1001 Balance: " +
                walletService.getAccountInfo(1001L).getBalance());
        System.out.println("1003 Balance: " +
                walletService.getAccountInfo(1003L).getBalance());

        // ===============================
        // FAULT TOLERANCE DEMO
        // ===============================
        System.out.println("\n--- Simulating leader failure ---");

        walletService.simulateLeaderFailure(0);

        walletService.transfer(1002L, 1001L, 2000);

        System.out.println("1001 Balance: " +
                walletService.getAccountInfo(1001L).getBalance());
        System.out.println("1002 Balance: " +
                walletService.getAccountInfo(1002L).getBalance());

        System.out.println("System still operational after leader failure");
    }
}
