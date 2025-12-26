package org.example.account;

import wallet.partition.WalletPartition;
import java.util.concurrent.atomic.AtomicLong;

public class FundTransfers {

    // Transaction ID generator is using for ordering (first-come-first-served)
    private static final AtomicLong transactionIdGenerator = new AtomicLong(0);

    /**
     * Same partition Transfers: Atomic with replication and allow concurrent transfers across different partitions
     * Uses locking to ensure FIFO ordering
     */
    public static void transfer(WalletPartition partition, long from, long to, double amount) {
        long transactionId = transactionIdGenerator.incrementAndGet();
        partition.lock();
        try {
            System.out.println("[Txn " + transactionId + "] Same Partition Transfer: " + from + " -> " + to + " (Amount: " + amount + ")");
            
            var leader = partition.getReplicaGroup().getLeader();
            Account src = leader.store.get(from);
            Account dst = leader.store.get(to);

            if (src == null || dst == null)
                throw new IllegalArgumentException("Invalid account");
            if (src.getBalance() < amount)
                throw new IllegalStateException("Insufficient balance");
            try {
                src.debit(amount);
                dst.credit(amount);
                partition.getReplicaGroup().replicate(src);
                partition.getReplicaGroup().replicate(dst);
                
                System.out.println("[Txn " + transactionId + "] Same Partition Transfer completed successfully");
            } catch (Exception e) {
                // Rollback on failure
                try {
                    src.credit(amount);
                    dst.debit(amount);
                    partition.getReplicaGroup().replicate(src);
                    partition.getReplicaGroup().replicate(dst);
                } catch (Exception rollbackError) {
                    System.err.println("[Txn " + transactionId + "] Rollback failed: " + rollbackError.getMessage());
                }
                throw new RuntimeException("Transaction failed and rolled back: " + e.getMessage(), e);
            }
        } finally {
            partition.unlock();
        }
    }

    /**
     * Cross Partition Transfers
     * Uses ordered locking to prevent deadlocks (always lock partitions in order)
     */
    public static void transfer(
        WalletPartition sourceAccountPartition,
        WalletPartition destinationAccountPartition,
        long fromAcc,
        long toAcc,
        double amount) {

        long transactionId = transactionIdGenerator.incrementAndGet();
        // Use fair locking to ensure FIFO ordering
        sourceAccountPartition.lock();
        try {
            destinationAccountPartition.lock();
            try {
                System.out.println("[Txn " + transactionId + "] Cross Partition Transfer: " + fromAcc + " -> " + toAcc + " (Amount: " + amount + ")");
                
                boolean debitCommitted = false;
                try {
                    Account srcAccount = sourceAccountPartition.getAccount(fromAcc);
                    Account dstAccount = destinationAccountPartition.getAccount(toAcc);
                    
                    if (srcAccount.getBalance() < amount) {
                        throw new IllegalStateException("Insufficient funds in source account");
                    }
                    System.out.println("[Txn " + transactionId + "] Cross Partition Transfer is being processed. Additional charge may apply.");
                    srcAccount.debit(amount);
                    sourceAccountPartition.getReplicaGroup().replicate(srcAccount);
                    debitCommitted = true; // debit was successful. need to do credit account reduction
                    dstAccount.credit(amount);
                    destinationAccountPartition.getReplicaGroup().replicate(dstAccount);
                    System.out.println("[Txn " + transactionId + "] Cross Partition Transfer completed successfully");
                    
                } catch (Exception e) {
                    // Rollback
                    if (debitCommitted) {
                        try {
                            Account srcAccount = sourceAccountPartition.getAccount(fromAcc);
                            srcAccount.credit(amount);
                            sourceAccountPartition.getReplicaGroup().replicate(srcAccount);
                            System.out.println("[Txn " + transactionId + "] Rollback completed - source account restored");
                        } catch (Exception rollbackError) {
                            System.err.println("[Txn " + transactionId + "] CRITICAL: Rollback failed: " + rollbackError.getMessage());
                            System.err.println("[Txn " + transactionId + "] Source account may be inconsistent!");
                        }
                    }
                    throw new RuntimeException("Cross-partition transaction aborted: " + e.getMessage(), e);
                }
            } finally {
                destinationAccountPartition.unlock();
            }
        } finally {
            sourceAccountPartition.unlock();
        }
    }
}

