#!/bin/bash

# Usage: ./run-replica.sh <partition-id> <replica-index> [is-leader] [base-port] [replica-urls]
# Example: ./run-replica.sh 0 0 true 8080 "http://localhost:8081,http://localhost:8082"
# Example: ./run-replica.sh 0 1 false 8081 "http://localhost:8080,http://localhost:8082"
# Example: ./run-replica.sh 0 2 false 8082 "http://localhost:8080,http://localhost:8081"
# Example: ./run-replica.sh 1 0 true 8090 "http://localhost:8091,http://localhost:8092"
# Example: ./run-replica.sh 1 1 false 8091 "http://localhost:8090,http://localhost:8092"
# Example: ./run-replica.sh 1 2 false 8092 "http://localhost:8090,http://localhost:8091"
#!/bin/bash

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: ./run-replica.sh <partition-id> <replica-index> [is-leader] [base-port] [replica-urls]"
    exit 1
fi

PARTITION_ID=$1
REPLICA_INDEX=$2
IS_LEADER=${3:-false}
BASE_PORT=${4:-8080}
REPLICA_URLS=${5:-""}

PORT=$((BASE_PORT + REPLICA_INDEX))
JAR_PATH="target/CW2-BankingSystemManagement-1.0-SNAPSHOT.jar"
echo $JAR_PATH

if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: JAR not found. Build the project first."
    echo "Run: mvn clean package"
    exit 1
fi

ARGS="\
--wallet.replica.partition.id=$PARTITION_ID \
--wallet.replica.index=$REPLICA_INDEX \
--wallet.replica.is.leader=$IS_LEADER \
--server.port=$PORT"

if [ -n "$REPLICA_URLS" ]; then
    ARGS="$ARGS --wallet.replica.urls=$REPLICA_URLS"
fi

echo "Starting Replica on port $PORT..."
java -jar "$JAR_PATH" $ARGS
