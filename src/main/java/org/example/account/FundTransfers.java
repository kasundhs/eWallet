package org.example.account;

import wallet.partition.WalletPartition;

public class FundTransfers {

    // Same partition Transfers
    public static synchronized void transfer(WalletPartition partition, long from, long to, double amount) {

        var leader = partition.getReplicaGroup().getLeader();
        Account src = leader.store.get(from);
        Account dst = leader.store.get(to);

        if (src == null || dst == null)
            throw new IllegalArgumentException("Invalid account");

        // Atomic section (single leader)
        System.out.println("Same Partition Transfers. No Additional fee Apply");
        src.debit(amount);
        dst.credit(amount);

        // Replicate updated state
        partition.getReplicaGroup().replicate(src);
        partition.getReplicaGroup().replicate(dst);
    }

    // Cross Partition Transfers
    public static synchronized void transfer(
        WalletPartition sourceAccountPartition,
        WalletPartition destinationAccountPartition,
        long fromAcc,
        long toAcc,
        double amount) {

        try {
            // Phase 1: Prepare
            if (sourceAccountPartition.getAccount(fromAcc).getBalance() < amount)
                throw new IllegalStateException("Insufficient funds");

            // Phase 2: Commit
            System.out.println("Cross Partition Transfer is being Processing. Additional Charge Maybe Apply");
            sourceAccountPartition.getAccount(fromAcc).debit(amount);
            destinationAccountPartition.getAccount(toAcc).credit(amount);

        } catch (Exception e) {
            throw new RuntimeException("Transaction aborted");
        }
    }
}

