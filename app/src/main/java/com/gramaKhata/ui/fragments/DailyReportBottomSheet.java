package com.gramaKhata.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.gramaKhata.databinding.DialogDailyReportBinding;

public class DailyReportBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_REPORT = "arg_report";

    private DialogDailyReportBinding binding;

    public static DailyReportBottomSheet newInstance(String report) {
        DailyReportBottomSheet sheet = new DailyReportBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_REPORT, report);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = DialogDailyReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String report = getArguments() == null ? "" : getArguments().getString(ARG_REPORT, "");
        binding.tvReportContent.setText(report);
        binding.btnShareReport.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, report);
            startActivity(Intent.createChooser(shareIntent, null));
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
