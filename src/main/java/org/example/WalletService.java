package org.example;

import org.example.Account;
import wallet.partition.PartitionResolver;
import wallet.partition.WalletPartition;
import wallet.replication.PartitionReplica;
import wallet.replication.ReplicaGroup;
import wallet.partition.TwoPhaseCommitService;

import java.util.ArrayList;
import java.util.List;

public class WalletService {

    private final List<WalletPartition> partitions;
    private final PartitionResolver resolver;

    public WalletService(int numberOfPartitions) {

        this.partitions = new ArrayList<>();
        this.resolver = new PartitionResolver(numberOfPartitions);

        for (int i = 0; i < numberOfPartitions; i++) {

            // ---- create replicas ----
            PartitionReplica r1 = new PartitionReplica(true);   // leader
            PartitionReplica r2 = new PartitionReplica(false);
            PartitionReplica r3 = new PartitionReplica(false);

            ReplicaGroup replicaGroup =
                    new ReplicaGroup(List.of(r1, r2, r3));

            partitions.add(new WalletPartition(replicaGroup));
        }
    }

    // ===============================
    // Account Creation (Clerk)
    // ===============================
    public void createAccount(long accountNumber, String name, String nic, double balance) {
        int partitionId = resolver.resolve(accountNumber);
        partitions.get(partitionId)
                .createAccount(accountNumber, name, nic, balance);
    }

    // ===============================
    // Balance / Info Inquiry
    // ===============================
    public Account getAccountInfo(long accountNumber) {
        int partitionId = resolver.resolve(accountNumber);
        return partitions.get(partitionId)
                .getAccount(accountNumber);
    }

    // ===============================
    // Fund Transfer
    // ===============================
    public void transfer(long from, long to, double amount) {

        int p1 = resolver.resolve(from);
        int p2 = resolver.resolve(to);

        if (p1 == p2) {
            // Same-partition transfer
            partitions.get(p1).transfer(from, to, amount);
        } else {
            // Cross-partition transfer (2PC)
            TwoPhaseCommitService.transfer(
                    partitions.get(p1),
                    partitions.get(p2),
                    from,
                    to,
                    amount
            );
        }
    }
    public void simulateLeaderFailure(int partitionId) {
        partitions.get(partitionId)
                .getReplicaGroup()
                .getLeader()
                .alive = false;
    }

}
