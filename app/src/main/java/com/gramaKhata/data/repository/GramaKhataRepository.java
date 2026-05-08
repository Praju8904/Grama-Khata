package com.gramaKhata.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.gramaKhata.data.db.AppDatabase;
import com.gramaKhata.data.db.CustomerDao;
import com.gramaKhata.data.db.CustomerEntity;
import com.gramaKhata.data.db.TransactionDao;
import com.gramaKhata.data.db.TransactionEntity;
import com.gramaKhata.utils.AppExecutors;

import java.util.List;

public class GramaKhataRepository {

    private final CustomerDao customerDao;
    private final TransactionDao transactionDao;
    private final LiveData<List<CustomerDao.CustomerWithBalance>> allCustomersWithBalance;

    public GramaKhataRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        customerDao = database.customerDao();
        transactionDao = database.transactionDao();
        allCustomersWithBalance = customerDao.getCustomersWithNetBalance();
    }

    public LiveData<List<CustomerDao.CustomerWithBalance>> getAllCustomersWithBalance() {
        return allCustomersWithBalance;
    }

    public LiveData<CustomerEntity> getCustomerById(int id) {
        MediatorLiveData<CustomerEntity> result = new MediatorLiveData<>();
        result.addSource(customerDao.getCustomerById(id), result::setValue);
        return result;
    }

    public LiveData<List<TransactionEntity>> getTransactionsForCustomer(int customerId) {
        MediatorLiveData<List<TransactionEntity>> result = new MediatorLiveData<>();
        result.addSource(transactionDao.getTransactionsForCustomer(customerId), result::setValue);
        return result;
    }

    public LiveData<Double> getNetBalance(int customerId) {
        MediatorLiveData<Double> result = new MediatorLiveData<>();
        result.addSource(transactionDao.getNetBalance(customerId), result::setValue);
        return result;
    }

    public void insertCustomer(CustomerEntity customer) {
        AppExecutors.getInstance().diskIO().execute(() -> customerDao.insertCustomer(customer));
    }

    public void updateCustomer(CustomerEntity customer) {
        AppExecutors.getInstance().diskIO().execute(() -> customerDao.updateCustomer(customer));
    }

    public void deleteCustomer(CustomerEntity customer) {
        AppExecutors.getInstance().diskIO().execute(() -> customerDao.deleteCustomer(customer));
    }

    public void insertTransaction(TransactionEntity transaction) {
        AppExecutors.getInstance().diskIO().execute(() -> transactionDao.insertTransaction(transaction));
    }

    public void deleteTransaction(TransactionEntity transaction) {
        AppExecutors.getInstance().diskIO().execute(() -> transactionDao.deleteTransaction(transaction));
    }
}
