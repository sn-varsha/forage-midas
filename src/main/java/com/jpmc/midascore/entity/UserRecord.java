package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- Added strategy
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private float balance;

    @OneToMany(mappedBy = "sender")
    private Set<Transaction> transactionsSent;

    @OneToMany(mappedBy = "recipient")
    private Set<Transaction> transactionsReceived;

    protected UserRecord() {
    }

    public UserRecord(String name, float balance) {
        this.name = name;
        this.balance = balance;
    }

    @Override
    public String toString() {
        // Updated toString for better logging
        return "UserRecord[id=" + id + ", name='" + name + "', balance=" + balance + "]";
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public Set<Transaction> getTransactionsSent() {
        return transactionsSent;
    }

    public void setTransactionsSent(Set<Transaction> transactionsSent) {
        this.transactionsSent = transactionsSent;
    }

    public Set<Transaction> getTransactionsReceived() {
        return transactionsReceived;
    }

    public void setTransactionsReceived(Set<Transaction> transactionsReceived) {
        this.transactionsReceived = transactionsReceived;
    }
}