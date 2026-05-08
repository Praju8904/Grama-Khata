package com.gramaKhata.data.ai;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.gramaKhata.data.db.TransactionEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiService {

    public interface GeminiCallback {
        void onSuccess(String result);

        void onError(String error);
    }

    private static final String MODEL_NAME = "gemini-1.5-flash";

    private final GenerativeModelFutures model;
    private final Executor backgroundExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GeminiService(String apiKey) {
        GenerativeModel generativeModel = new GenerativeModel(MODEL_NAME, apiKey);
        model = GenerativeModelFutures.from(generativeModel);
        backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    public void suggestReminderMessage(
            String customerName,
            String shopName,
            double amount,
            GeminiCallback callback
    ) {
        ensureCallback(callback);
        String prompt = String.format(
                Locale.US,
                "Write a polite, short WhatsApp reminder in Kannada and English for a village "
                        + "shopkeeper named %s to send to their customer %s who owes ₹%.2f. "
                        + "Keep it friendly and under 2 sentences.",
                safeText(shopName),
                safeText(customerName),
                amount
        );
        runPrompt(prompt, callback);
    }

    public void analyzeCustomerRisk(List<TransactionEntity> transactions, GeminiCallback callback) {
        ensureCallback(callback);
        String summary = buildTransactionSummary(transactions);
        String prompt = "Based on this transaction history for a village grocery store customer, "
                + "give a 1-sentence risk assessment: Low Risk / Medium Risk / High Risk and one "
                + "sentence of reasoning.\n\n"
                + summary;
        runPrompt(prompt, callback);
    }

    public void generateSmartNote(double amount, String type, GeminiCallback callback) {
        ensureCallback(callback);
        String prompt = String.format(
                Locale.US,
                "Suggest a short 3-5 word transaction note for a %s of ₹%.2f in a village "
                        + "grocery store. Return only the note, nothing else.",
                safeText(type),
                amount
        );
        runPrompt(prompt, callback);
    }

    private void runPrompt(String prompt, GeminiCallback callback) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result == null || result.getText() == null || result.getText().trim().isEmpty()) {
                    postError(callback, "Gemini returned an empty response");
                    return;
                }
                postSuccess(callback, result.getText().trim());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                String message = throwable.getMessage();
                postError(callback, message == null ? "Gemini request failed" : message);
            }
        }, backgroundExecutor);
    }

    private String buildTransactionSummary(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return "No transactions available.";
        }

        List<TransactionEntity> sorted = new ArrayList<>(transactions);
        Collections.sort(sorted, Comparator.comparingLong(TransactionEntity::getTimestamp).reversed());
        int count = Math.min(10, sorted.size());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < count; i++) {
            TransactionEntity transaction = sorted.get(i);
            if (transaction == null) {
                continue;
            }
            summary.append(i + 1)
                    .append(". ")
                    .append(dateFormat.format(new Date(transaction.getTimestamp())))
                    .append(" - ")
                    .append(safeText(transaction.getType()))
                    .append(" - ₹")
                    .append(String.format(Locale.US, "%.2f", transaction.getAmount()));

            String note = transaction.getNote();
            if (note != null && !note.trim().isEmpty()) {
                summary.append(" (").append(note.trim()).append(")");
            }
            summary.append('\n');
        }

        if (summary.length() == 0) {
            return "No transactions available.";
        }
        return summary.toString().trim();
    }

    private void postSuccess(GeminiCallback callback, String result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postError(GeminiCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private void ensureCallback(GeminiCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
