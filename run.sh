#!/bin/bash

echo "========================================"
echo "Starting eWallet System Backend"
echo "========================================"
echo ""

cd "$(dirname "$0")"

echo "Building project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Starting Spring Boot application..."
echo "Frontend will be available at: http://localhost:8080"
echo "API endpoints at: http://localhost:8080/api"
echo ""

mvn spring-boot:run -Dspring-boot.run.main-class=org.example.WalletApplication

