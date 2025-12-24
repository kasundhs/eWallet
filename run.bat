@echo off
echo ========================================
echo Starting eWallet System Backend
echo ========================================
echo.

cd /d %~dp0

echo Building project...
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application...
echo Frontend will be available at: http://localhost:8080
echo API endpoints at: http://localhost:8080/api
echo.

call mvn spring-boot:run -Dspring-boot.run.main-class=org.example.WalletApplication

pause

