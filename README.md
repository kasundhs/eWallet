# eWallet System - Microservices Architecture

This is a microservices-based eWallet system with high availability through replication and partitioning.

## Architecture Overview

The system consists of:

1. **Wallet Service** (Port 8080): Core service handling account creation and fund transfers
   - REST API endpoints for accounts and transfers
   - Partitioned data storage for scalability
   - Replication for high availability (leader-follower pattern)
   - CORS enabled for multiple frontend connections
   - Direct API access (no separate gateway needed for single-service setup)

**Note**: In a production microservices environment, you would typically deploy an API Gateway (like Spring Cloud Gateway, Zuul, or Kong) as a separate service. For this implementation, the REST APIs are accessed directly.

## Features

- **Account Management**: Create accounts and query account information
- **Fund Transfers**: Transfer funds between accounts (same-partition and cross-partition)
- **High Availability**: Automatic leader election on replica failure
- **Partitioning**: Accounts distributed across multiple partitions
- **Replication**: Each partition has 3 replicas (1 leader, 2 followers)
- **RESTful API**: Standard REST endpoints for all operations

## Running the Application

### 1. Build the Project

```bash
mvn clean install
```

### 2. Run Wallet Service

```bash
mvn spring-boot:run -Dspring-boot.run.main-class=org.example.WalletApplication
```

Or:
```bash
java -jar target/CW2-BankingSystemManagement-1.0-SNAPSHOT.jar
```

The wallet service will start on port 8080.

### 3. Access the APIs

Services are available directly at:
- Wallet Service: http://localhost:8080

For a true microservices setup with API Gateway, deploy the gateway as a separate service that routes to this service.

## API Endpoints

### Account Operations

#### Create Account
```
POST /api/accounts
Content-Type: application/json

{
  "accountNumber": 1001,
  "holderName": "John Doe",
  "nic": "199812345678",
  "balance": 50000.00
}
```

#### Get Account Info
```
GET /api/accounts/{accountNumber}
```

### Transfer Operations

#### Transfer Funds
```
POST /api/transfers
Content-Type: application/json

{
  "fromAccount": 1001,
  "toAccount": 1002,
  "amount": 5000.00
}
```

## Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port (default: 8080)
- Number of partitions (default: 2)

## Frontend Integration

The API supports CORS and can be accessed from any frontend application. Example using fetch:

```javascript
// Create Account
fetch('http://localhost:8080/api/accounts', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    accountNumber: 1001,
    holderName: "John Doe",
    nic: "199812345678",
    balance: 50000.00
  })
});

// Transfer Funds
fetch('http://localhost:8080/api/transfers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    fromAccount: 1001,
    toAccount: 1002,
    amount: 5000.00
  })
});
```

## High Availability

The system automatically handles:
- Leader failure detection
- Automatic leader election
- State replication across replicas
- Cross-partition transaction handling

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Web (REST APIs)
- Spring Cloud Gateway (API Gateway)
- Maven

