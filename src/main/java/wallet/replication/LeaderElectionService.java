package wallet.replication;

import org.example.account.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that monitors heartbeat and performs leader election
 */
@Service
public class LeaderElectionService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${wallet.replica.partition.id:0}")
    private int partitionId;
    
    @Value("${wallet.replica.index:0}")
    private int replicaIndex;
    
    @Value("${wallet.replica.is.leader:false}")
    private volatile boolean isLeader;
    
    @Value("${wallet.replica.urls:}")
    private String replicaUrls;
    
    @Autowired(required = false)
    private ReplicaHealthService replicaHealthService;
    
    @Autowired(required = false)
    private WalletService walletService;
    
    private final List<String> replicaUrlList = new java.util.ArrayList<>();
    private volatile String currentLeaderUrl = null;

    @jakarta.annotation.PostConstruct
    public void initialize() {
        if (replicaUrls != null && !replicaUrls.isEmpty()) {
            String[] urls = replicaUrls.split(",");
            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    replicaUrlList.add(trimmedUrl);
                }
            }
        }
        
        // If this replica is configured as leader, it's the initial leader
        if (isLeader) {
            System.out.println("[LeaderElectionService] This replica is configured as the initial leader");
        }
    }

    /**
     * Check leader status and perform election if needed
     * Runs every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void checkLeaderAndElect() {
        if (replicaHealthService == null || walletService == null) {
            return;
        }
        
        if (isLeader) {
            // This replica is the leader, no need to check
            return;
        }
        
        // Check if any replica with lower index (potential leader) is alive
        // Assumption: replica with index 0 is the initial leader
        boolean shouldBecomeLeader = checkIfShouldBecomeLeader();
        
        if (shouldBecomeLeader) {
            // All replicas with lower index are down, this replica should become leader
            System.out.println("[LeaderElectionService] All replicas with lower index are not responding. Promoting to leader...");
            promoteToLeader();
        }
    }
    
    private boolean checkIfShouldBecomeLeader() {
        // Simple leader election: if all replicas with index < this.replicaIndex are down,
        // this replica becomes the leader
        List<String> replicaUrls = replicaHealthService.getReplicaUrlList();
        
        // Check if any replica with lower index is alive
        // In a real implementation, you'd parse the replica index from the URL or response
        // For simplicity, assume replica index corresponds to port order
        // If we can't reach any replicas, or all are down, this replica can become leader
        boolean hasAliveReplica = false;
        for (String url : replicaUrls) {
            if (replicaHealthService.isReplicaHealthy(url)) {
                hasAliveReplica = true;
                break;
            }
        }
        
        // If no replicas are alive, this replica can become leader
        // In a more sophisticated implementation, you'd check replica indices
        return !hasAliveReplica;
    }
    
    private void promoteToLeader() {
        // In a real system, you'd implement a distributed consensus algorithm (like Raft)
        // For simplicity, just set this replica as leader
        // Note: This is a simplified implementation and doesn't handle split-brain scenarios
        isLeader = true;
        System.out.println("\n[LEADER ELECTION] This replica (Partition " + partitionId + ", Replica " + replicaIndex + ") is now the LEADER");
        System.out.println("[LEADER ELECTION] Leader election completed. This replica will handle requests.");
        
        // Update the replica's leader status in the partition
        if (walletService != null) {
            walletService.updateLeaderStatus(true);
        }
    }
    
    public boolean isLeader() {
        return isLeader;
    }
    
    public void setLeader(boolean leader) {
        this.isLeader = leader;
    }
}

