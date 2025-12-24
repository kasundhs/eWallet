package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.partition.PartitionResolver;
import wallet.partition.WalletPartition;
import wallet.replication.PartitionReplica;
import wallet.replication.ReplicaGroup;

import java.util.ArrayList;
import java.util.List;

@Service
public class WalletService {

    private final List<WalletPartition> partitions;
    private final PartitionResolver resolver;

    public WalletService(@Value("${wallet.partitions:2}") int numberOfPartitions) {

        this.partitions = new ArrayList<>();
        this.resolver = new PartitionResolver(numberOfPartitions);

        for (int i = 0; i < numberOfPartitions; i++) {

            // ---- create replicas ----
            PartitionReplica r1 = new PartitionReplica(true);   // leader
            PartitionReplica r2 = new PartitionReplica(false);
            PartitionReplica r3 = new PartitionReplica(false);

            ReplicaGroup replicaGroup = new ReplicaGroup(List.of(r1, r2, r3)); // Set replicas standby

            partitions.add(new WalletPartition(replicaGroup));
        }
    }

    // ===============================
    // Account Creation (Clerk)
    // ===============================
    public void createAccount(long accountNumber, String name, String nic, double balance) {
        int partitionId = resolver.resolvePartitionId(accountNumber);
        partitions.get(partitionId).createAccount(accountNumber, name, nic, balance);
    }

    // ===============================
    // Balance / Info Inquiry
    // ===============================
    public Account getAccountInfo(long accountNumber) {
        int partitionId = resolver.resolvePartitionId(accountNumber);
        return partitions.get(partitionId).getAccount(accountNumber);
    }

    // ===============================
    // Fund Transfer
    // ===============================
    public void transfer(long fromAcc, long toAcc, double amount) {

        int p1 = resolver.resolvePartitionId(fromAcc);
        int p2 = resolver.resolvePartitionId(toAcc);

        if (p1 == p2) {
            // Same-partition transfer
            FundTransfers.transfer(partitions.get(p1),fromAcc, toAcc, amount);
        } else {
            // Cross-partition transfer (between 2 partitions)
            FundTransfers.transfer(
                    partitions.get(p1),
                    partitions.get(p2),
                    fromAcc,
                    toAcc,
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
