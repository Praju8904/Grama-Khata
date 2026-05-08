package com.gramaKhata.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insertTransaction(TransactionEntity t);

    @Delete
    void deleteTransaction(TransactionEntity t);

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    LiveData<List<TransactionEntity>> getTransactionsForCustomer(int customerId);

    @Query("SELECT SUM(CASE WHEN type='CREDIT' THEN amount ELSE -amount END) FROM transactions WHERE customerId = :customerId")
    LiveData<Double> getNetBalance(int customerId);
}
