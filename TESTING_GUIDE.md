# Testing Guide - eWallet System

This guide explains how to test the eWallet System, including both backend APIs and the frontend interface.

## Prerequisites

1. **Java 17** or higher installed
2. **Maven** installed
3. **Web browser** (Chrome, Firefox, Edge, etc.)

## Step 1: Build and Start the Backend

### Option A: Using Maven

```bash
# Navigate to project root
cd D:\Concurrent\CW2-eWalletSystem

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run -Dspring-boot.run.main-class=org.example.WalletApplication
```

### Option B: Using Java directly

```bash
# Build the project first
mvn clean package

# Run the JAR file
java -jar target/CW2-BankingSystemManagement-1.0-SNAPSHOT.jar
```

The backend service will start on **http://localhost:8080**

You should see output like:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started WalletApplication in X.XXX seconds
```

## Step 2: Access the Frontend

Once the backend is running, open your web browser and navigate to:

**http://localhost:8080**

The frontend interface will automatically load (it's served as a static resource from Spring Boot).

## Step 3: Testing the Frontend Interface

### Test 1: Create Account

1. Click on the **"Create Account"** tab (it's active by default)
2. Fill in the form:
   - Account Number: `1001`
   - Holder Name: `John Doe`
   - NIC: `199812345678`
   - Initial Balance: `50000.00`
3. Click **"Create Account"**
4. You should see a success message and the account details displayed below

### Test 2: View Account

1. Click on the **"View Account"** tab
2. Enter an account number (e.g., `1001`)
3. Click **"View Account"**
4. The account information should be displayed

### Test 3: Transfer Funds

1. First, create two accounts:
   - Account 1001 with balance 50000
   - Account 1002 with balance 30000
2. Click on the **"Transfer Funds"** tab
3. Fill in:
   - From Account: `1001`
   - To Account: `1002`
   - Amount: `5000`
4. Click **"Transfer Funds"**
5. You should see a success message
6. Verify by viewing both accounts to see updated balances

### Test 4: Cross-Partition Transfer

To test cross-partition transfers:
1. Create accounts with numbers that will be in different partitions
   - Account 1001 (will be in partition 0: 1001 % 2 = 1)
   - Account 1002 (will be in partition 0: 1002 % 2 = 0)
   - Account 1003 (will be in partition 1: 1003 % 2 = 1)
2. Transfer from account 1001 to account 1002 (cross-partition)
3. Check the console/backend logs for "Cross Partition Transfer" message

## Step 4: Testing with cURL (Command Line)

### Create Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"accountNumber\": 1001,
    \"holderName\": \"John Doe\",
    \"nic\": \"199812345678\",
    \"balance\": 50000.00
  }"
```

### Get Account Info

```bash
curl http://localhost:8080/api/accounts/1001
```

### Transfer Funds

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d "{
    \"fromAccount\": 1001,
    \"toAccount\": 1002,
    \"amount\": 5000.00
  }"
```

## Step 5: Testing with Postman

1. **Import/Set up Postman**
   - Open Postman
   - Create a new collection called "eWallet System"

2. **Create Account Request**
   - Method: `POST`
   - URL: `http://localhost:8080/api/accounts`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "accountNumber": 1001,
       "holderName": "John Doe",
       "nic": "199812345678",
       "balance": 50000.00
     }
     ```

3. **Get Account Request**
   - Method: `GET`
   - URL: `http://localhost:8080/api/accounts/1001`

4. **Transfer Funds Request**
   - Method: `POST`
   - URL: `http://localhost:8080/api/transfers`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "fromAccount": 1001,
       "toAccount": 1002,
       "amount": 5000.00
     }
     ```

## Step 6: Testing Error Cases

### Test Invalid Account Creation

Try creating an account with:
- Duplicate account number (should fail)
- Negative balance (if validation is added)

### Test Invalid Transfer

1. Try transferring more than the account balance
2. Try transferring from a non-existent account
3. Try transferring to a non-existent account

All should return appropriate error messages.

## Step 7: Testing High Availability (Leader Failure)

To test the replication and leader election:

1. Use the backend code to simulate leader failure (this would require additional admin endpoints)
2. Or check the console logs when operations are performed
3. The system should automatically elect a new leader if the current one fails

## Troubleshooting

### Frontend not loading

- Make sure the backend is running on port 8080
- Check browser console for errors
- Verify CORS is enabled (it should be by default)

### API calls failing

- Check that the backend is running: `curl http://localhost:8080/actuator/health`
- Check backend logs for errors
- Verify the request format matches the API specification

### CORS errors

If you're accessing from a different origin:
- The backend has CORS configured to allow all origins
- If issues persist, check the `CorsConfig.java` file

## Health Check

Check if the service is running:

```bash
curl http://localhost:8080/actuator/health
```

Should return:
```json
{"status":"UP"}
```

## Test Scenarios Summary

| Test Case | Expected Result |
|-----------|----------------|
| Create account with valid data | Account created successfully |
| Create account with duplicate number | Error: Account already exists |
| View existing account | Account details displayed |
| View non-existent account | Error: Account not found |
| Transfer with sufficient balance | Transfer successful |
| Transfer with insufficient balance | Error: Insufficient funds |
| Transfer from non-existent account | Error: Invalid account |
| Cross-partition transfer | Transfer successful (with log message) |
| Same-partition transfer | Transfer successful (no additional fee message) |

## Performance Testing

For load testing, you can use tools like:
- **Apache JMeter**
- **Artillery**
- **wrk**

Example with curl in a loop:
```bash
# Create multiple accounts
for i in {1001..1100}; do
  curl -X POST http://localhost:8080/api/accounts \
    -H "Content-Type: application/json" \
    -d "{\"accountNumber\":$i,\"holderName\":\"User $i\",\"nic\":\"1998$i\",\"balance\":10000.00}"
done
```

