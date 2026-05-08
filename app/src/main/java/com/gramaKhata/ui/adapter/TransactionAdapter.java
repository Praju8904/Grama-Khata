package com.gramaKhata.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.gramaKhata.R;
import com.gramaKhata.data.db.TransactionEntity;
import com.gramaKhata.databinding.ItemTransactionBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnDeleteListener {
        void onDelete(TransactionEntity transaction);
    }

    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private final List<TransactionEntity> transactions = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public TransactionAdapter(List<TransactionEntity> transactions, OnDeleteListener deleteListener) {
        if (transactions != null) {
            this.transactions.addAll(transactions);
        }
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(transactions.get(position), deleteListener);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateList(List<TransactionEntity> newList) {
        List<TransactionEntity> safeNewList = newList == null
                ? Collections.emptyList()
                : new ArrayList<>(newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new TransactionDiffCallback(transactions, safeNewList)
        );
        transactions.clear();
        transactions.addAll(safeNewList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {

        private final ItemTransactionBinding binding;

        TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TransactionEntity transaction, OnDeleteListener listener) {
            String type = transaction.getType() == null ? "" : transaction.getType();
            boolean isCredit = "CREDIT".equalsIgnoreCase(type);

            binding.tvTypeChip.setText(type);
            binding.tvTypeChip.setBackgroundResource(
                    isCredit ? R.drawable.bg_chip_credit : R.drawable.bg_chip_payment
            );

            String note = transaction.getNote();
            if (TextUtils.isEmpty(note)) {
                binding.tvNote.setVisibility(View.GONE);
            } else {
                binding.tvNote.setVisibility(View.VISIBLE);
                binding.tvNote.setText(note);
            }

            binding.tvTimestamp.setText(DATE_TIME_FORMAT.format(new Date(transaction.getTimestamp())));
            binding.tvAmount.setText("₹" + String.format(Locale.US, "%.2f", transaction.getAmount()));
            binding.ibDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(transaction);
                }
            });
        }
    }

    static class TransactionDiffCallback extends DiffUtil.Callback {

        private final List<TransactionEntity> oldList;
        private final List<TransactionEntity> newList;

        TransactionDiffCallback(List<TransactionEntity> oldList, List<TransactionEntity> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TransactionEntity oldItem = oldList.get(oldItemPosition);
            TransactionEntity newItem = newList.get(newItemPosition);
            return oldItem.getId() == newItem.getId()
                    && oldItem.getCustomerId() == newItem.getCustomerId()
                    && oldItem.getTimestamp() == newItem.getTimestamp()
                    && Double.compare(oldItem.getAmount(), newItem.getAmount()) == 0
                    && TextUtils.equals(oldItem.getType(), newItem.getType())
                    && TextUtils.equals(oldItem.getNote(), newItem.getNote());
        }
    }
}
