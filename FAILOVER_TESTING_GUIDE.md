# Failover Testing Guide

This guide explains how to test the high availability and failover mechanisms in the eWallet System.

## Overview

The system uses a leader-follower replication pattern where:
- Each partition has 3 replicas (1 leader, 2 followers)
- When the leader fails, a new leader is automatically elected from the remaining alive replicas
- The system can tolerate up to 2 replica failures (needs at least 1 alive replica)

## Testing Methods

### Method 1: Using the Web UI (Recommended)

1. **Start the application:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.main-class=org.example.WalletApplication
   ```

2. **Navigate to the Failover Testing page:**
   ```
   http://localhost:8080/failover-test.html
   ```

3. **Use the interactive UI to:**
   - View real-time partition and replica status
   - Simulate leader failures
   - Run automated test scenarios
   - Monitor failover events

### Method 2: Using REST API

#### Check Partition Status

```bash
# Get status of all partitions
curl http://localhost:8080/api/admin/partitions/status

# Get status of a specific partition
curl http://localhost:8080/api/admin/partitions/0/status
```

#### Simulate Leader Failure

```bash
# Simulate leader failure for partition 0
curl -X POST http://localhost:8080/api/admin/partitions/0/failover
```

#### Simulate Replica Failure

```bash
# Simulate failure of replica 1 in partition 0
curl -X POST http://localhost:8080/api/admin/partitions/0/replicas/1/fail
```

### Method 3: Using cURL Test Scripts

#### Test Scenario 1: Basic Leader Failover

```bash
# 1. Check initial status
curl http://localhost:8080/api/admin/partitions/0/status

# 2. Simulate leader failure
curl -X POST http://localhost:8080/api/admin/partitions/0/failover

# 3. Check status after failover (new leader should be elected)
curl http://localhost:8080/api/admin/partitions/0/status

# 4. Perform an operation to trigger leader election
curl http://localhost:8080/api/accounts/1001
```

#### Test Scenario 2: Failover During Operations

```bash
# 1. Create an account
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": 5001,
    "holderName": "Test User",
    "nic": "199850000001",
    "balance": 10000.00
  }'

# 2. Simulate leader failure
curl -X POST http://localhost:8080/api/admin/partitions/1/failover

# 3. Try to access the account (should work with new leader)
curl http://localhost:8080/api/accounts/5001

# 4. Perform a transfer (should work with new leader)
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": 5001,
    "toAccount": 5002,
    "amount": 1000.00
  }'
```

#### Test Scenario 3: Multiple Replica Failures

```bash
# 1. Check initial status
curl http://localhost:8080/api/admin/partitions/0/status

# 2. Fail a follower replica (replica 1)
curl -X POST http://localhost:8080/api/admin/partitions/0/replicas/1/fail

# 3. Check status (should show 2 alive replicas)
curl http://localhost:8080/api/admin/partitions/0/status

# 4. Fail the leader
curl -X POST http://localhost:8080/api/admin/partitions/0/failover

# 5. Check status (should show new leader, 1 alive replica)
curl http://localhost:8080/api/admin/partitions/0/status

# 6. System should still work with 1 alive replica
curl http://localhost:8080/api/accounts/1001
```

## Expected Behavior

### When Leader Fails:

1. **Leader Election:**
   - System detects leader is not alive
   - Automatically elects a new leader from alive followers
   - Console logs show: `[FAILOVER] New leader elected: Replica X`

2. **Operation Continuity:**
   - All operations continue to work seamlessly
   - No data loss (replication ensures consistency)
   - Response times may be slightly higher during failover

3. **API Response:**
   - Status endpoint shows new leader index
   - Operations succeed with new leader
   - No errors or timeouts

### When All Replicas Fail:

- System throws: `IllegalStateException: No replica available - all replicas are down!`
- Operations fail until at least one replica recovers

## Monitoring Failover

### Console Output

Watch the console/terminal where the application is running. You'll see:

```
=== Simulating Leader Failure for Partition 0 ===
Leader marked as failed. New leader will be elected on next operation.

[FAILOVER] New leader elected: Replica 1
[FAILOVER] Leader is now handling requests.
```

### API Status Response

The status API returns detailed information:

```json
{
  "success": true,
  "data": {
    "partitionId": 0,
    "totalReplicas": 3,
    "currentLeaderIndex": 1,
    "aliveReplicasCount": 2,
    "replicas": [
      {
        "index": 0,
        "isLeader": false,
        "isAlive": false,
        "accountsCount": 5
      },
      {
        "index": 1,
        "isLeader": true,
        "isAlive": true,
        "accountsCount": 5
      },
      {
        "index": 2,
        "isLeader": false,
        "isAlive": true,
        "accountsCount": 5
      }
    ]
  }
}
```

## Test Scenarios

### Scenario 1: Single Leader Failure ✅

**Steps:**
1. Verify initial leader (should be Replica 0)
2. Simulate leader failure
3. Perform any operation (triggers leader election)
4. Verify new leader is elected
5. Confirm operations continue to work

**Expected:** New leader elected, operations continue seamlessly

### Scenario 2: Multiple Replica Failures ✅

**Steps:**
1. Fail one follower replica
2. Fail the leader
3. Verify new leader elected from remaining alive replicas
4. Confirm system still operational

**Expected:** System handles up to 2 failures, new leader elected

### Scenario 3: Failover During Active Operations ✅

**Steps:**
1. Create accounts
2. Perform transfers
3. Simulate leader failure mid-operation
4. Continue operations
5. Verify data consistency

**Expected:** Operations complete successfully, no data loss

### Scenario 4: Sequential Failures ✅

**Steps:**
1. Fail replica 1
2. Fail replica 2
3. Fail replica 0 (current leader)
4. System should fail (no replicas left)

**Expected:** System throws exception when all replicas fail

## Verification Checklist

After running failover tests, verify:

- [ ] New leader is automatically elected
- [ ] Operations continue to work after failover
- [ ] No data loss occurs
- [ ] Console shows failover logs
- [ ] Status API reflects new leader
- [ ] Multiple failures are handled correctly
- [ ] System fails gracefully when all replicas are down

## Troubleshooting

### Failover Not Happening?

- Ensure you're performing an operation after simulating failure (failover is lazy - happens on next operation)
- Check console logs for error messages
- Verify partition ID is correct (0 or 1 for default 2-partition setup)

### Operations Failing After Failover?

- Check if at least one replica is still alive
- Verify the new leader was elected successfully
- Check console logs for exceptions

### Status Not Updating?

- Refresh the status endpoint
- Wait a moment for leader election to complete
- Check console for error messages

## Notes

- Leader election is **lazy** - it happens when the next operation requests the leader
- Replication ensures all alive replicas have the same data
- The system can tolerate **N-1 failures** (N = total replicas per partition)
- Default configuration: 2 partitions, 3 replicas per partition

