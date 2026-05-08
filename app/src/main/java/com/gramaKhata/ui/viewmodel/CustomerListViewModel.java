package com.gramaKhata.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.gramaKhata.GramaKhataApplication;
import com.gramaKhata.data.db.CustomerDao;
import com.gramaKhata.data.db.CustomerEntity;
import com.gramaKhata.data.repository.GramaKhataRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CustomerListViewModel extends AndroidViewModel {

    private final GramaKhataRepository repository;
    private final LiveData<List<CustomerDao.CustomerWithBalance>> allCustomers;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<CustomerDao.CustomerWithBalance>> filteredCustomers =
            new MediatorLiveData<>();

    public CustomerListViewModel(@NonNull Application application) {
        super(application);
        repository = GramaKhataApplication.getRepository();
        allCustomers = repository.getAllCustomersWithBalance();

        filteredCustomers.addSource(allCustomers,
                customers -> updateFilteredCustomers(customers, searchQuery.getValue()));
        filteredCustomers.addSource(searchQuery,
                query -> updateFilteredCustomers(allCustomers.getValue(), query));
    }

    public LiveData<List<CustomerDao.CustomerWithBalance>> getFilteredCustomers() {
        return filteredCustomers;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query);
    }

    public void deleteCustomer(CustomerEntity customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer cannot be null");
        }
        repository.deleteCustomer(customer);
    }

    public LiveData<Double> getTotalOutstanding() {
        return Transformations.map(allCustomers, customers -> {
            if (customers == null || customers.isEmpty()) {
                return 0d;
            }
            double total = 0d;
            for (CustomerDao.CustomerWithBalance customer : customers) {
                if (customer != null && customer.netBalance > 0) {
                    total += customer.netBalance;
                }
            }
            return total;
        });
    }

    private void updateFilteredCustomers(List<CustomerDao.CustomerWithBalance> customers, String query) {
        if (customers == null || customers.isEmpty()) {
            filteredCustomers.setValue(Collections.emptyList());
            return;
        }

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<CustomerDao.CustomerWithBalance> result = new ArrayList<>();
        for (CustomerDao.CustomerWithBalance customer : customers) {
            if (customer == null) {
                continue;
            }
            String name = customer.name == null ? "" : customer.name;
            if (normalizedQuery.isEmpty() || name.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                result.add(customer);
            }
        }
        result.sort(Comparator.comparingDouble(
                (CustomerDao.CustomerWithBalance c) -> c.netBalance).reversed());
        filteredCustomers.setValue(result);
    }
}
