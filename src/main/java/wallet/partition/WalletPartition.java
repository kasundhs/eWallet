package wallet.partition;

import org.example.account.Account;
import wallet.replication.ReplicaGroup;
import java.util.concurrent.locks.ReentrantLock;

public class WalletPartition {

    private final ReplicaGroup replicaGroup;
    // Fair lock ensures first-come-first-served ordering for concurrent transactions
    private final ReentrantLock partitionLock = new ReentrantLock(true);

    public WalletPartition(ReplicaGroup replicaGroup) {
        this.replicaGroup = replicaGroup;
    }
    
    /**
     * Acquires the partition lock. This ensures fair, first-come-first-served ordering
     * for concurrent transactions on the same partition.
     */
    public void lock() {
        partitionLock.lock();
    }
    
    /**
     * Releases the partition lock.
     */
    public void unlock() {
        partitionLock.unlock();
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
