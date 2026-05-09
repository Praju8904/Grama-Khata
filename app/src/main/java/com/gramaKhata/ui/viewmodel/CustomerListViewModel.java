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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CustomerListViewModel extends AndroidViewModel {

    private final GramaKhataRepository repository;
    private final LiveData<List<CustomerDao.CustomerWithBalance>> allCustomers;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Timeframe> timeframeFilter = new MutableLiveData<>(Timeframe.TODAY);
    private final MediatorLiveData<List<CustomerDao.CustomerWithBalance>> filteredCustomers =
            new MediatorLiveData<>();
            
    public enum Timeframe {
        TODAY,
        YESTERDAY,
        DAY_BEFORE_YESTERDAY,
        ALL_TIME
    }

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
    
    public void setTimeframeFilter(Timeframe timeframe) {
        if (timeframe != null && timeframeFilter.getValue() != timeframe) {
            timeframeFilter.setValue(timeframe);
        }
    }

    public void deleteCustomer(CustomerEntity customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer cannot be null");
        }
        repository.deleteCustomer(customer);
    }

    public LiveData<Double> getTotalOutstanding() {
        return Transformations.switchMap(timeframeFilter, timeframe -> {
            long startTime = 0;
            long endTime = Long.MAX_VALUE;
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            if (timeframe == Timeframe.TODAY) {
                startTime = cal.getTimeInMillis();
            } else if (timeframe == Timeframe.YESTERDAY) {
                endTime = cal.getTimeInMillis() - 1;
                cal.add(Calendar.DAY_OF_YEAR, -1);
                startTime = cal.getTimeInMillis();
            } else if (timeframe == Timeframe.DAY_BEFORE_YESTERDAY) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
                endTime = cal.getTimeInMillis() - 1;
                cal.add(Calendar.DAY_OF_YEAR, -1);
                startTime = cal.getTimeInMillis();
            }
            return repository.getTotalOutstandingBetween(startTime, endTime);
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
