package wallet.replication;

import org.example.Account;
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
        for (PartitionReplica replica : replicas) {
            if (replica.alive) {
                replica.isLeader = true;
                leader = replica;
                System.out.println("New leader selected : "+leader);
                return;
            }
        }
        throw new IllegalStateException("No replica available");
    }

    public synchronized void replicate(Account account) {
        for (PartitionReplica replica : replicas) {
            if (replica.alive) {
                replica.store.put(account.accountNumber, account);
            }
        }
    }
}

