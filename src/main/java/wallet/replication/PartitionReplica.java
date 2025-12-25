package wallet.replication;

import org.example.account.Account;
import java.util.HashMap;
import java.util.Map;

public class PartitionReplica {

    public boolean isLeader;
    public boolean alive = true;
    public final Map<Long, Account> store = new HashMap<>();

    public PartitionReplica(boolean isLeader) {
        this.isLeader = isLeader;
    }
}
