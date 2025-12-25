package wallet.replication;

import org.example.account.Account;
import java.util.List;

public class ReplicaGroup {

    private PartitionReplica leader;
    private final List<PartitionReplica> replicas;

    public ReplicaGroup(List<PartitionReplica> replicas) {
        this.replicas = replicas;
        this.leader = replicas.stream().filter(replica -> replica.isLeader).findFirst().get();
    }

    public synchronized PartitionReplica getLeader() {
        if (!leader.alive) {
            electNewLeader();
        }
        return leader;
    }

    private void electNewLeader() {
        // Mark all replicas as non-leaders first
        for (PartitionReplica replica : replicas) {
            replica.isLeader = false;
        }
        
        // Find first alive replica and make it leader
        for (PartitionReplica replica : replicas) {
            if (replica.alive) {
                replica.isLeader = true;
                leader = replica;
                System.out.println("\n[FAILOVER] New leader elected: Replica " + getReplicaIndex(replica));
                System.out.println("[FAILOVER] Leader is now handling requests.");
                return;
            }
        }
        throw new IllegalStateException("No replica available - all replicas are down!");
    }

    private int getReplicaIndex(PartitionReplica target) {
        for (int i = 0; i < replicas.size(); i++) {
            if (replicas.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    public List<PartitionReplica> getReplicas() {
        return replicas;
    }

    public synchronized void replicate(Account account) {
        for (PartitionReplica replica : replicas) {
            if (replica.alive) {
                replica.store.put(account.accountNumber, account);
            }
        }
    }
}

