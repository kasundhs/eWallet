# eWallet Distributed System

A distributed wallet system with partition-based data storage and replica-based high availability.

## Architecture

- **2 Partitions**: Data is partitioned based on account number (modulo 2)
- **3 Replicas per Partition**: Each partition has 3 replicas (1 leader + 2 followers)
- **Heartbeat Checking**: Replicas check each other's health every 3 seconds
- **Leader Election**: If the primary (leader) doesn't respond to heartbeat, a new leader is automatically elected

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean install -DskipTests
```

Or simply use the run script (it builds automatically).

## Running the System

Each replica must run as a **separate process**. Start each replica in a separate terminal window.

### For Partition 0 (3 replicas):

**Terminal 1 - Replica 0 (Leader):**
```bash
./run-replica.sh 0 0 true 8080 "http://localhost:8081,http://localhost:8082"
```

**Terminal 2 - Replica 1 (Follower):**
```bash
./run-replica.sh 0 1 false 8081 "http://localhost:8080,http://localhost:8082"
```

**Terminal 3 - Replica 2 (Follower):**
```bash
./run-replica.sh 0 2 false 8082 "http://localhost:8080,http://localhost:8081"
```

### For Partition 1 (if needed, 3 more replicas):

**Terminal 4 - Replica 0 (Leader):**
```bash
./run-replica.sh 1 0 true 8090 "http://localhost:8091,http://localhost:8092"
```

**Terminal 5 - Replica 1 (Follower):**
```bash
./run-replica.sh 1 1 false 8091 "http://localhost:8090,http://localhost:8092"
```

**Terminal 6 - Replica 2 (Follower):**
```bash
./run-replica.sh 1 2 false 8092 "http://localhost:8090,http://localhost:8091"
```

## Script Parameters

1. **partition-id**: The partition this replica belongs to (0 or 1)
2. **replica-index**: The replica index within the partition (0, 1, or 2)
3. **is-leader**: `true` if this is the initial leader, `false` otherwise
4. **base-port**: Base port number (default: 8080). Actual port = base-port + replica-index
5. **replica-urls**: Comma-separated URLs of OTHER replicas to monitor
   - Do NOT include this replica's own URL
   - Include URLs of other replicas in the same partition
   - With 3 replicas, each replica monitors 2 others (hence 2 URLs)

## Frontend Access

Once the replicas are running, access the frontend at:
- **Main UI**: `http://localhost:8080` (connect to the leader replica on port 8080)
- **Failover Testing**: `http://localhost:8080/failover-test.html`

## Features

- **Heartbeat Checking**: Each replica periodically checks the health of other replicas (every 3 seconds)
- **Automatic Leader Election**: If the leader fails, the next available replica automatically becomes the leader (within 5 seconds)
- **Health Endpoint**: Each replica exposes `/api/replica/health` for heartbeat checks
- **Replica Info**: Each replica exposes `/api/replica/info` for status information

## Testing Leader Failover

1. Start all 3 replicas for a partition (as shown above)
2. The replica with `is-leader=true` becomes the initial leader
3. Stop the leader replica (Ctrl+C)
4. Within 5 seconds, one of the follower replicas will automatically promote itself to leader
5. Check the logs to see the leader election message:
   ```
   [LEADER ELECTION] This replica (Partition X, Replica Y) is now the LEADER
   ```

## API Endpoints

- `POST /api/accounts` - Create account
- `GET /api/accounts/{accountNumber}` - Get account info
- `POST /api/transfers` - Transfer funds
- `GET /api/admin/partitions/status` - Get all partitions status
- `GET /api/admin/partitions/{partitionId}/status` - Get partition status
- `GET /api/replica/health` - Health check endpoint
- `GET /api/replica/info` - Replica information

## Configuration

Configuration is done via command-line arguments when starting replicas:

- `wallet.partitions` - Total number of partitions (default: 2)
- `wallet.replica.partition.id` - **Required**: Partition ID this replica belongs to
- `wallet.replica.index` - **Required**: Replica index within partition
- `wallet.replica.is.leader` - Whether this replica is the initial leader (default: false)
- `wallet.replica.urls` - Comma-separated URLs of other replicas to monitor
- `server.port` - Port number for this replica

## Notes

- Each replica manages only ONE partition
- Cross-partition transfers are not supported when replicas run separately
- Only same-partition transfers are supported within each replica
- Each replica runs on a different port to avoid conflicts

