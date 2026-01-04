package wallet.replication;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ReplicaHealthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ConcurrentMap<String, Boolean> replicaHealthStatus = new ConcurrentHashMap<>();
    
    @Value("${wallet.replica.partition.id:0}")
    private int partitionId;
    
    @Value("${wallet.replica.index:0}")
    private int replicaIndex;
    
    @Value("${wallet.replica.urls:}")
    private String replicaUrls;
    
    private final List<String> replicaUrlList = new ArrayList<>();

    @PostConstruct
    public void initializeReplicaUrls() {
        if (replicaUrls != null && !replicaUrls.isEmpty()) {
            String[] urls = replicaUrls.split(",");
            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    replicaUrlList.add(trimmedUrl);
                }
            }
        }
        System.out.println("[ReplicaHealthService] Initialized with " + replicaUrlList.size() + " replica URLs for partition " + partitionId + ", replica " + replicaIndex);
    }

    /**
     * Check heartbeat of other replicas every 3 seconds
     */
    @Scheduled(fixedRate = 3000)
    public void checkReplicaHealth() {
        for (String replicaUrl : replicaUrlList) {
            try {
                String healthUrl = replicaUrl + "/api/replica/health";
                String response = restTemplate.getForObject(healthUrl, String.class);
                replicaHealthStatus.put(replicaUrl, true);
            } catch (RestClientException e) {
                replicaHealthStatus.put(replicaUrl, false);
                System.out.println("[HEARTBEAT] Replica at " + replicaUrl + " is not responding");
            }
        }
    }

    public boolean isReplicaHealthy(String replicaUrl) {
        return replicaHealthStatus.getOrDefault(replicaUrl, false);
    }

    public List<String> getReplicaUrlList() {
        return new ArrayList<>(replicaUrlList);
    }

    public int getPartitionId() {
        return partitionId;
    }

    public int getReplicaIndex() {
        return replicaIndex;
    }
}

