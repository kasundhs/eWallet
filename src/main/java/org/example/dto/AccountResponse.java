package org.example.dto;

public class AccountResponse {
    private long accountNumber;
    private String holderName;
    private String nic;
    private double balance;

    public AccountResponse() {
    }

    public AccountResponse(long accountNumber, String holderName, String nic, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.nic = nic;
        this.balance = balance;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

