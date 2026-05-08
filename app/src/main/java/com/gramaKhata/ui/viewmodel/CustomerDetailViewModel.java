package com.gramaKhata.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gramaKhata.GramaKhataApplication;
import com.gramaKhata.data.db.CustomerEntity;
import com.gramaKhata.data.db.TransactionEntity;
import com.gramaKhata.data.repository.GramaKhataRepository;
import com.gramaKhata.utils.DateUtils;

import java.util.List;
import java.util.Locale;

public class CustomerDetailViewModel extends AndroidViewModel {

    private final GramaKhataRepository repository;
    private int customerId = -1;
    private LiveData<CustomerEntity> customer;
    private LiveData<List<TransactionEntity>> transactions;
    private LiveData<Double> netBalance;

    public CustomerDetailViewModel(@NonNull Application application) {
        super(application);
        repository = GramaKhataApplication.getRepository();
    }

    public void setCustomerId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("customer id must be positive");
        }
        if (customerId == id && customer != null && transactions != null && netBalance != null) {
            return;
        }
        customerId = id;
        customer = repository.getCustomerById(customerId);
        transactions = repository.getTransactionsForCustomer(customerId);
        netBalance = repository.getNetBalance(customerId);
    }

    public LiveData<CustomerEntity> getCustomer() {
        return customer;
    }

    public LiveData<List<TransactionEntity>> getTransactions() {
        return transactions;
    }

    public LiveData<Double> getNetBalance() {
        return netBalance;
    }

    public void addTransaction(double amount, String type, String note) {
        if (customerId <= 0) {
            throw new IllegalStateException("setCustomerId must be called before addTransaction");
        }
        TransactionEntity transaction = new TransactionEntity();
        transaction.setCustomerId(customerId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setNote(note);
        transaction.setTimestamp(System.currentTimeMillis());
        repository.insertTransaction(transaction);
    }

    public void deleteTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction cannot be null");
        }
        repository.deleteTransaction(transaction);
    }

    public String generateWhatsAppMessage(String shopName) {
        double due = 0d;
        if (netBalance != null && netBalance.getValue() != null) {
            due = netBalance.getValue();
        }
        return "Namaskara, your due at "
                + safeText(shopName)
                + " is ₹"
                + String.format(Locale.US, "%.2f", due)
                + ". Please pay at your earliest convenience.";
    }

    public String generateDailyReport(String shopName) {
        List<TransactionEntity> currentTransactions = transactions == null ? null : transactions.getValue();
        StringBuilder lines = new StringBuilder();
        double totalCredited = 0d;
        double totalCollected = 0d;

        if (currentTransactions != null) {
            for (TransactionEntity transaction : currentTransactions) {
                if (transaction == null || !DateUtils.isToday(transaction.getTimestamp())) {
                    continue;
                }
                String txType = transaction.getType() == null
                        ? "UNKNOWN"
                        : transaction.getType().trim().toUpperCase(Locale.ROOT);
                double amount = transaction.getAmount();

                if ("CREDIT".equals(txType)) {
                    totalCredited += amount;
                } else {
                    totalCollected += amount;
                }

                lines.append(txType)
                        .append(": ₹")
                        .append(String.format(Locale.US, "%.2f", amount));

                String note = transaction.getNote();
                if (note != null && !note.trim().isEmpty()) {
                    lines.append(" (").append(note.trim()).append(")");
                }
                lines.append('\n');
            }
        }

        if (lines.length() == 0) {
            lines.append("No transactions today.\n");
        }

        return "--- Grama-Khata Daily Report ---\n"
                + "Date: " + DateUtils.todayFormatted() + "\n"
                + "Shop: " + safeText(shopName) + "\n\n"
                + lines
                + "---\n"
                + "Total Credited: ₹" + String.format(Locale.US, "%.2f", totalCredited) + "\n"
                + "Total Collected: ₹" + String.format(Locale.US, "%.2f", totalCollected) + "\n"
                + "---";
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }
}
