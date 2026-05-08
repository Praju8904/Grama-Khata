package com.gramaKhata.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.gramaKhata.GramaKhataApplication;
import com.gramaKhata.data.db.CustomerEntity;
import com.gramaKhata.data.repository.GramaKhataRepository;

public class AddEditCustomerViewModel extends AndroidViewModel {

    private final GramaKhataRepository repository;
    private LiveData<CustomerEntity> customerSource;
    private Observer<CustomerEntity> customerObserver;
    private long existingCreatedAt = -1L;

    public final MutableLiveData<String> name = new MutableLiveData<>("");
    public final MutableLiveData<String> phone = new MutableLiveData<>("");
    public final MutableLiveData<String> photoUri = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    public final MutableLiveData<String> validationError = new MutableLiveData<>();

    public AddEditCustomerViewModel(@NonNull Application application) {
        super(application);
        repository = GramaKhataApplication.getRepository();
    }

    public void loadCustomer(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("customer id must be positive");
        }
        detachObserver();
        customerSource = repository.getCustomerById(customerId);
        customerObserver = customer -> {
            if (customer == null) {
                return;
            }
            name.postValue(customer.getName());
            phone.postValue(customer.getPhone());
            photoUri.postValue(customer.getPhotoUri());
            existingCreatedAt = customer.getCreatedAt();
        };
        customerSource.observeForever(customerObserver);
    }

    public void saveCustomer(int existingId) {
        String normalizedName = normalize(name.getValue());
        String normalizedPhone = normalize(phone.getValue());

        if (normalizedName.isEmpty()) {
            validationError.postValue("Customer name is required");
            return;
        }
        if (normalizedPhone.isEmpty()) {
            validationError.postValue("Phone number is required");
            return;
        }
        validationError.postValue(null);

        if (existingId == -1) {
            CustomerEntity customer = new CustomerEntity(
                    normalizedName,
                    normalizeNullable(photoUri.getValue()),
                    normalizedPhone
            );
            repository.insertCustomer(customer);
            saveSuccess.postValue(true);
            return;
        }

        CustomerEntity updated = new CustomerEntity();
        updated.setId(existingId);
        updated.setName(normalizedName);
        updated.setPhone(normalizedPhone);
        updated.setPhotoUri(normalizeNullable(photoUri.getValue()));

        long createdAtToUse = existingCreatedAt;
        if (createdAtToUse <= 0 && customerSource != null && customerSource.getValue() != null) {
            createdAtToUse = customerSource.getValue().getCreatedAt();
        }
        if (createdAtToUse <= 0) {
            throw new IllegalStateException("Missing createdAt for customer id: " + existingId);
        }
        updated.setCreatedAt(createdAtToUse);

        repository.updateCustomer(updated);
        saveSuccess.postValue(true);
    }

    public void setPhotoUri(String uri) {
        photoUri.setValue(uri == null ? "" : uri);
    }

    public void deleteCustomer(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("customer id must be positive");
        }
        LiveData<CustomerEntity> source = repository.getCustomerById(customerId);
        Observer<CustomerEntity> oneShotObserver = new Observer<CustomerEntity>() {
            @Override
            public void onChanged(CustomerEntity customer) {
                if (customer == null) {
                    source.removeObserver(this);
                    throw new IllegalStateException("Customer not found for id: " + customerId);
                }
                repository.deleteCustomer(customer);
                source.removeObserver(this);
            }
        };
        source.observeForever(oneShotObserver);
    }

    @Override
    protected void onCleared() {
        detachObserver();
        super.onCleared();
    }

    private void detachObserver() {
        if (customerSource != null && customerObserver != null) {
            customerSource.removeObserver(customerObserver);
        }
        customerSource = null;
        customerObserver = null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
