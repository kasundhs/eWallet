package org.example.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.partition.PartitionResolver;
import wallet.partition.WalletPartition;
import wallet.replication.PartitionReplica;
import wallet.replication.ReplicaGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    private final List<WalletPartition> partitions;
    private final PartitionResolver resolver;
    private final int replicaPartitionId;

    public WalletService(
            @Value("${wallet.partitions:2}") int numberOfPartitions,
            @Value("${wallet.replica.partition.id}") int replicaPartitionId,
            @Value("${wallet.replica.index}") int replicaIndex,
            @Value("${wallet.replica.is.leader:false}") boolean isLeader) {
        
        if (replicaPartitionId < 0 || replicaPartitionId >= numberOfPartitions) {
            throw new IllegalArgumentException("Replica partition ID must be between 0 and " + (numberOfPartitions - 1));
        }
        
        this.replicaPartitionId = replicaPartitionId;
        this.partitions = new ArrayList<>();
        this.resolver = new PartitionResolver(numberOfPartitions);

        // Create only this partition with a single replica
        PartitionReplica replica = new PartitionReplica(isLeader);
        ReplicaGroup replicaGroup = new ReplicaGroup(List.of(replica));
        
        // Create empty partitions for other partition IDs (for resolver compatibility)
        for (int i = 0; i < numberOfPartitions; i++) {
            if (i == replicaPartitionId) {
                partitions.add(new WalletPartition(replicaGroup));
            } else {
                // Create empty partition (won't be used but keeps indices consistent)
                PartitionReplica dummyReplica = new PartitionReplica(false);
                ReplicaGroup dummyGroup = new ReplicaGroup(List.of(dummyReplica));
                partitions.add(new WalletPartition(dummyGroup));
            }
        }
        
        System.out.println("[WalletService] Replica initialized: Partition " + replicaPartitionId + ", Replica " + replicaIndex + ", Leader: " + isLeader);
    }
    
    /**
     * Update the leader status of this replica
     */
    public void updateLeaderStatus(boolean isLeader) {
        var replicaGroup = partitions.get(replicaPartitionId).getReplicaGroup();
        var replicas = replicaGroup.getReplicas();
        if (!replicas.isEmpty()) {
            replicas.get(0).isLeader = isLeader;
            System.out.println("[WalletService] Leader status updated: " + isLeader);
        }
    }

    // ===============================
    // Account Creation (Clerk)
    // ===============================
    public void createAccount(long accountNumber, String name, String nic, double balance) {
        partitions.get(replicaPartitionId).createAccount(accountNumber, name, nic, balance);
    }

    // ===============================
    // Balance / Info Inquiry
    // ===============================
    public Account getAccountInfo(long accountNumber) {
        return partitions.get(replicaPartitionId).getAccount(accountNumber);
    }

    // ===============================
    // Fund Transfer
    // ===============================
    public void transfer(long fromAcc, long toAcc, double amount) {
        FundTransfers.transfer(partitions.get(replicaPartitionId), fromAcc, toAcc, amount);
    }

    public void simulateLeaderFailure(int partitionId) {
        if (partitionId < 0 || partitionId >= partitions.size()) {
            throw new IllegalArgumentException("Invalid partition ID: " + partitionId);
        }
        System.out.println("\n=== Simulating Leader Failure for Partition " + partitionId + " ===");
        partitions.get(partitionId)
                .getReplicaGroup()
                .getLeader()
                .alive = false;
        System.out.println("Leader marked as failed. New leader will be elected on next operation.");
    }

    public void simulateReplicaFailure(int partitionId, int replicaIndex) {
        if (partitionId < 0 || partitionId >= partitions.size()) {
            throw new IllegalArgumentException("Invalid partition ID: " + partitionId);
        }
        var replicaGroup = partitions.get(partitionId).getReplicaGroup();
        var replicas = replicaGroup.getReplicas();
        if (replicaIndex < 0 || replicaIndex >= replicas.size()) {
            throw new IllegalArgumentException("Invalid replica index: " + replicaIndex);
        }
        System.out.println("\n=== Simulating Replica " + replicaIndex + " Failure for Partition " + partitionId + " ===");
        replicas.get(replicaIndex).alive = false;
        System.out.println("Replica " + replicaIndex + " marked as failed.");
    }

    public Map<String, Object> getPartitionStatus(int partitionId) {
        if (partitionId < 0 || partitionId >= partitions.size()) {
            throw new IllegalArgumentException("Invalid partition ID: " + partitionId);
        }
        
        var replicaGroup = partitions.get(partitionId).getReplicaGroup();
        var replicas = replicaGroup.getReplicas();
        var leader = replicaGroup.getLeader();
        
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("partitionId", partitionId);
        status.put("totalReplicas", replicas.size());

        List<Map<String, Object>> replicaStatus = new ArrayList<>();
        for (int i = 0; i < replicas.size(); i++) {
            var replica = replicas.get(i);
            Map<String, Object> replicaInfo = new java.util.HashMap<>();
            replicaInfo.put("index", i);
            replicaInfo.put("isLeader", replica.isLeader);
            replicaInfo.put("isAlive", replica.alive);
            replicaInfo.put("accountsCount", replica.store.size());
            replicaStatus.add(replicaInfo);
        }
        status.put("replicas", replicaStatus);
        
        // Find leader index
        int leaderIndex = -1;
        for (int i = 0; i < replicas.size(); i++) {
            if (replicas.get(i) == leader) {
                leaderIndex = i;
                break;
            }
        }
        status.put("currentLeaderIndex", leaderIndex);
        status.put("aliveReplicasCount", replicas.stream().mapToInt(r -> r.alive ? 1 : 0).sum());
        
        return status;
    }

    public Map<String, Object> getAllPartitionsStatus() {
        Map<String, Object> allStatus = new java.util.HashMap<>();
        allStatus.put("totalPartitions", partitions.size());
        
        java.util.List<Map<String, Object>> partitionsStatus = new java.util.ArrayList<>();
        for (int i = 0; i < partitions.size(); i++) {
            partitionsStatus.add(getPartitionStatus(i));
        }
        allStatus.put("partitions", partitionsStatus);
        
        return allStatus;
    }

}
