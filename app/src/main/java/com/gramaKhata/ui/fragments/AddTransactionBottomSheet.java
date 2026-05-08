package com.gramaKhata.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.gramaKhata.R;
import com.gramaKhata.databinding.DialogAddTransactionBinding;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    public interface OnTransactionSavedListener {
        void onTransactionSaved(double amount, String type, String note);
    }

    private static final String ARG_TYPE = "arg_type";
    private static final String TYPE_CREDIT = "CREDIT";
    private static final String TYPE_PAYMENT = "PAYMENT";

    private DialogAddTransactionBinding binding;
    private String selectedType = TYPE_CREDIT;
    private OnTransactionSavedListener listener;

    public static AddTransactionBottomSheet newInstance(String type) {
        AddTransactionBottomSheet sheet = new AddTransactionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnTransactionSavedListener(OnTransactionSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = DialogAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String initialType = getArguments() == null ? TYPE_CREDIT : getArguments().getString(ARG_TYPE);
        selectedType = TYPE_PAYMENT.equalsIgnoreCase(initialType) ? TYPE_PAYMENT : TYPE_CREDIT;
        binding.toggleType.check(TYPE_PAYMENT.equals(selectedType) ? R.id.btn_payment : R.id.btn_credit);
        binding.tvTitle.setText(TYPE_PAYMENT.equals(selectedType)
                ? R.string.add_payment
                : R.string.add_credit);

        binding.toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            selectedType = checkedId == R.id.btn_payment ? TYPE_PAYMENT : TYPE_CREDIT;
            binding.tvTitle.setText(TYPE_PAYMENT.equals(selectedType)
                    ? R.string.add_payment
                    : R.string.add_credit);
        });

        binding.btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String amountText = binding.etAmount.getText() == null
                ? ""
                : binding.etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountText)) {
            binding.etAmount.setError(getString(R.string.amount_required));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException exception) {
            binding.etAmount.setError(getString(R.string.amount_invalid));
            return;
        }
        if (amount <= 0) {
            binding.etAmount.setError(getString(R.string.amount_invalid));
            return;
        }
        if (listener == null) {
            throw new IllegalStateException("OnTransactionSavedListener is not set");
        }

        String note = binding.etNote.getText() == null
                ? ""
                : binding.etNote.getText().toString().trim();
        listener.onTransactionSaved(amount, selectedType, note);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
