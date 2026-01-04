package wallet.replication;

import org.example.account.Account;
import java.util.List;

public class ReplicaGroup {

    private final PartitionReplica replica;

    public ReplicaGroup(List<PartitionReplica> replicas) {
        if (replicas.size() != 1) {
            throw new IllegalArgumentException("ReplicaGroup must contain exactly one replica");
        }
        this.replica = replicas.get(0);
    }

    public synchronized PartitionReplica getLeader() {
        // The replica itself is the leader
        // Leader status is managed externally via heartbeat/leader election
        return replica;
    }

    public List<PartitionReplica> getReplicas() {
        return List.of(replica);
    }
    
    /**
     * Replicate account data to local store
     * (In distributed mode, this would replicate to other replicas via network)
     */
    public synchronized void replicate(Account account) {
        replica.store.put(account.accountNumber, account);
    }
}

