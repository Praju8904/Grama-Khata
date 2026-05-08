package com.gramaKhata.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomerDao {

    @Insert
    long insertCustomer(CustomerEntity customer);

    @Update
    void updateCustomer(CustomerEntity customer);

    @Delete
    void deleteCustomer(CustomerEntity customer);

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    LiveData<List<CustomerEntity>> getAllCustomers();

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    LiveData<CustomerEntity> getCustomerById(int id);

    @Query("SELECT c.id, c.name, c.photoUri, c.phone, " +
            "COALESCE(SUM(CASE WHEN t.type='CREDIT' THEN t.amount ELSE -t.amount END), 0) AS netBalance " +
            "FROM customers c LEFT JOIN transactions t ON c.id = t.customerId " +
            "GROUP BY c.id ORDER BY netBalance DESC")
    LiveData<List<CustomerWithBalance>> getCustomersWithNetBalance();

    class CustomerWithBalance {
        public int id;
        public String name;
        public String photoUri;
        public String phone;
        public double netBalance;
    }
}
