package wallet.partition;

import org.example.account.Account;
import wallet.replication.ReplicaGroup;

public class WalletPartition {

    private final ReplicaGroup replicaGroup;

    public WalletPartition(ReplicaGroup replicaGroup) {
        this.replicaGroup = replicaGroup;
    }

    public synchronized void createAccount(long accNo, String name, String nic, double balance) {
        var leader = replicaGroup.getLeader();
        if (leader.store.containsKey(accNo))
            throw new IllegalArgumentException("Account already exists");

        Account account = new Account(accNo, name, nic, balance);
        replicaGroup.replicate(account);
    }
    public synchronized Account getAccount(long accNo) {
        Account acc = replicaGroup.getLeader().store.get(accNo);
        if (acc == null)
            throw new IllegalArgumentException("Account not found");
        return acc;
    }
    public ReplicaGroup getReplicaGroup() {
        return replicaGroup;
    }

}
