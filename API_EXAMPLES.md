# API Usage Examples

This document provides examples of how to use the eWallet System REST APIs.

## Base URL
- Development: `http://localhost:8080`

## Endpoints

### 1. Create Account

**Request:**
```http
POST /api/accounts
Content-Type: application/json

{
  "accountNumber": 1001,
  "holderName": "John Doe",
  "nic": "199812345678",
  "balance": 50000.00
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "accountNumber": 1001,
    "holderName": "John Doe",
    "nic": "199812345678",
    "balance": 50000.00
  }
}
```

### 2. Get Account Information

**Request:**
```http
GET /api/accounts/1001
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "accountNumber": 1001,
    "holderName": "John Doe",
    "nic": "199812345678",
    "balance": 50000.00
  }
}
```

### 3. Transfer Funds

**Request:**
```http
POST /api/transfers
Content-Type: application/json

{
  "fromAccount": 1001,
  "toAccount": 1002,
  "amount": 5000.00
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": "Transfer completed successfully from account 1001 to account 1002 for amount 5000.0"
}
```

## Error Responses

### Account Not Found (404)
```json
{
  "success": false,
  "message": "Account not found",
  "data": null
}
```

### Insufficient Funds (400)
```json
{
  "success": false,
  "message": "Insufficient balance",
  "data": null
}
```

### Invalid Account (400)
```json
{
  "success": false,
  "message": "Invalid account",
  "data": null
}
```

## Frontend Integration Examples

### JavaScript (Fetch API)

```javascript
// Create Account
async function createAccount(accountNumber, holderName, nic, balance) {
  const response = await fetch('http://localhost:8080/api/accounts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      accountNumber: accountNumber,
      holderName: holderName,
      nic: nic,
      balance: balance
    })
  });
  
  const result = await response.json();
  return result;
}

// Get Account Info
async function getAccountInfo(accountNumber) {
  const response = await fetch(`http://localhost:8080/api/accounts/${accountNumber}`);
  const result = await response.json();
  return result;
}

// Transfer Funds
async function transferFunds(fromAccount, toAccount, amount) {
  const response = await fetch('http://localhost:8080/api/transfers', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      fromAccount: fromAccount,
      toAccount: toAccount,
      amount: amount
    })
  });
  
  const result = await response.json();
  return result;
}
```

### cURL Examples

```bash
# Create Account
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": 1001,
    "holderName": "John Doe",
    "nic": "199812345678",
    "balance": 50000.00
  }'

# Get Account Info
curl http://localhost:8080/api/accounts/1001

# Transfer Funds
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": 1001,
    "toAccount": 1002,
    "amount": 5000.00
  }'
```

### Python Example

```python
import requests

BASE_URL = "http://localhost:8080"

# Create Account
def create_account(account_number, holder_name, nic, balance):
    url = f"{BASE_URL}/api/accounts"
    data = {
        "accountNumber": account_number,
        "holderName": holder_name,
        "nic": nic,
        "balance": balance
    }
    response = requests.post(url, json=data)
    return response.json()

# Get Account Info
def get_account_info(account_number):
    url = f"{BASE_URL}/api/accounts/{account_number}"
    response = requests.get(url)
    return response.json()

# Transfer Funds
def transfer_funds(from_account, to_account, amount):
    url = f"{BASE_URL}/api/transfers"
    data = {
        "fromAccount": from_account,
        "toAccount": to_account,
        "amount": amount
    }
    response = requests.post(url, json=data)
    return response.json()
```

