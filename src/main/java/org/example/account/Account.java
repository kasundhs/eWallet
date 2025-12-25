package org.example.account;

public class Account {

    public final long accountNumber;
    public final String holderName;
    public final String nic;
    private double balance;

    public Account(long accountNumber, String holderName, String nic, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.nic = nic;
        this.balance = balance;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void credit(double amount) {
        balance += amount;
    }

    public synchronized void printAccountDetails(){
        System.out.println("\n===============================\nAccount Details");
        System.out.println("Account Number : " + accountNumber);
        System.out.println("Holder Name    : " + holderName);
        System.out.println("NIC            : " + nic);
        System.out.println("Balance        : " + balance);
        System.out.println("===============================");
    }

    public synchronized void debit(double amount) {
        if (balance < amount)
            throw new IllegalStateException("Insufficient balance");
        balance -= amount;
    }
}
