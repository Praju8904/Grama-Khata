package com.gramaKhata.data.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "transactions",
        foreignKeys = @ForeignKey(
                entity = CustomerEntity.class,
                parentColumns = "id",
                childColumns = "customerId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "customerId")
)
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int customerId;
    private double amount;
    private String type;
    @Nullable
    private String note;
    private long timestamp = System.currentTimeMillis();

    public TransactionEntity() {
    }

    public TransactionEntity(int customerId, double amount, String type, @Nullable String note) {
        this.customerId = customerId;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Nullable
    public String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
