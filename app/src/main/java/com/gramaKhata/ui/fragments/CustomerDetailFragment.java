package com.gramaKhata.ui.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.gramaKhata.BuildConfig;
import com.gramaKhata.R;
import com.gramaKhata.data.db.CustomerEntity;
import com.gramaKhata.data.db.TransactionEntity;
import com.gramaKhata.databinding.FragmentCustomerDetailBinding;
import com.gramaKhata.ui.adapter.TransactionAdapter;
import com.gramaKhata.ui.viewmodel.CustomerDetailViewModel;
import com.gramaKhata.utils.AvatarUtils;
import com.gramaKhata.utils.CurrencyFormatter;
import com.gramaKhata.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDetailFragment extends Fragment {

    private static final String TYPE_CREDIT = "CREDIT";
    private static final String TYPE_PAYMENT = "PAYMENT";

    private FragmentCustomerDetailBinding binding;
    private CustomerDetailViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private int customerId = -1;
    private CustomerEntity currentCustomer;
    private double currentBalance = 0d;
    private List<TransactionEntity> currentTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentCustomerDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customerId = getArguments() == null ? -1 : getArguments().getInt("customerId", -1);
        if (customerId <= 0) {
            throw new IllegalArgumentException("customerId argument is required");
        }

        viewModel = new ViewModelProvider(this).get(CustomerDetailViewModel.class);
        viewModel.setCustomerId(customerId);

        setupToolbar();
        setupTransactions();
        setupButtons();
        observeData();
    }
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> findNavController().navigateUp());
        binding.toolbar.inflateMenu(R.menu.menu_customer_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_whatsapp) {
                String shopName = PreferencesManager.getShopName(requireContext());
                String message = viewModel.generateWhatsAppMessage(shopName);
                shareWithWhatsAppFallback(message);
                return true;
            }
            if (itemId == R.id.action_daily_report) {
                String shopName = PreferencesManager.getShopName(requireContext());
                String report = viewModel.generateDailyReport(shopName);
                DailyReportBottomSheet.newInstance(report)
                        .show(getChildFragmentManager(), "daily_report_sheet");
                return true;
            }
            if (itemId == R.id.action_edit) {
                Bundle args = new Bundle();
                args.putInt("customerId", customerId);
                findNavController().navigate(R.id.action_to_edit_customer, args);
                return true;
            }
            return false;
        });
    }

    private void setupTransactions() {
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), viewModel::deleteTransaction);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupButtons() {
        binding.btnAddCredit.setOnClickListener(v -> showAddTransactionSheet(TYPE_CREDIT));
        binding.btnAddPayment.setOnClickListener(v -> showAddTransactionSheet(TYPE_PAYMENT));
        binding.btnCall.setOnClickListener(v -> dialCustomer());
        binding.btnWhatsapp.setOnClickListener(v -> sendWhatsApp());
        binding.btnSms.setOnClickListener(v -> sendSms());
    }

    private void observeData() {
        viewModel.getCustomer().observe(getViewLifecycleOwner(), customer -> {
            currentCustomer = customer;
            if (customer == null) {
                return;
            }
            String name = safeText(customer.getName());
            binding.tvCustomerName.setText(name);
            binding.tvPhone.setText(safeText(customer.getPhone()));
            bindAvatar(name, customer.getPhotoUri());
        });

        viewModel.getNetBalance().observe(getViewLifecycleOwner(), netBalance -> {
            currentBalance = netBalance == null ? 0d : netBalance;
            binding.tvBalanceAmount.setText(CurrencyFormatter.format(currentBalance));
            int color = ContextCompat.getColor(
                    requireContext(),
                    currentBalance <= 0 ? R.color.colorCreditGreen : R.color.colorDebitRed
            );
            binding.tvBalanceAmount.setTextColor(color);
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            currentTransactions = transactions == null ? new ArrayList<>() : transactions;
            transactionAdapter.updateList(currentTransactions);
        });
    }

    private void dialCustomer() {
        if (currentCustomer == null || TextUtils.isEmpty(currentCustomer.getPhone())) {
            Snackbar.make(binding.getRoot(), R.string.phone_unavailable, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent dialIntent = new Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + currentCustomer.getPhone().trim())
        );
        startActivity(dialIntent);
    }

    private void showAddTransactionSheet(String type) {
        AddTransactionBottomSheet sheet = AddTransactionBottomSheet.newInstance(type);
        sheet.setOnTransactionSavedListener((amount, transactionType, note) ->
                viewModel.addTransaction(amount, transactionType, note));
        sheet.show(getChildFragmentManager(), "add_transaction_sheet");
    }

    private void sendWhatsApp() {
        if (currentCustomer == null || TextUtils.isEmpty(currentCustomer.getPhone())) {
            Snackbar.make(binding.getRoot(), R.string.phone_unavailable, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String shopName = PreferencesManager.getShopName(requireContext());
        String message = viewModel.generateWhatsAppMessage(shopName);
        String phone = currentCustomer.getPhone().replaceAll("[^0-9+]", "");
        if (!phone.startsWith("+") && phone.length() == 10) {
            phone = "+91" + phone;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            shareWithWhatsAppFallback(message);
        }
    }

    private void sendSms() {
        if (currentCustomer == null || TextUtils.isEmpty(currentCustomer.getPhone())) {
            Snackbar.make(binding.getRoot(), R.string.phone_unavailable, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String shopName = PreferencesManager.getShopName(requireContext());
        String message = viewModel.generateWhatsAppMessage(shopName);
        
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + currentCustomer.getPhone().trim()));
        intent.putExtra("sms_body", message);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(binding.getRoot(), "SMS app not found", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareWithWhatsAppFallback(String message) {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);
        whatsappIntent.setPackage("com.whatsapp");
        try {
            startActivity(whatsappIntent);
        } catch (ActivityNotFoundException exception) {
            Intent fallbackIntent = new Intent(Intent.ACTION_SEND);
            fallbackIntent.setType("text/plain");
            fallbackIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(fallbackIntent, getString(R.string.share_report)));
        }
    }



    private void bindAvatar(String name, String photoUri) {
        if (!TextUtils.isEmpty(photoUri)) {
            Glide.with(this)
                    .load(photoUri)
                    .circleCrop()
                    .into(binding.ivAvatarLarge);
            return;
        }
        Glide.with(this).clear(binding.ivAvatarLarge);
        int sizePx = binding.ivAvatarLarge.getLayoutParams() != null
                && binding.ivAvatarLarge.getLayoutParams().width > 0
                ? binding.ivAvatarLarge.getLayoutParams().width
                : dpToPx(80);
        binding.ivAvatarLarge.setImageBitmap(createInitialAvatar(name, sizePx));
    }

    private Bitmap createInitialAvatar(String name, int sizePx) {
        int safeSize = Math.max(sizePx, 1);
        Bitmap bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(AvatarUtils.getColorForName(name));
        canvas.drawCircle(safeSize / 2f, safeSize / 2f, safeSize / 2f, backgroundPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(safeSize * 0.42f);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float y = (safeSize / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(AvatarUtils.getInitial(name), safeSize / 2f, y, textPaint);
        return bitmap;
    }

    private int dpToPx(int dp) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        requireContext().getResources().getDisplayMetrics()
                )
        );
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private NavController findNavController() {
        return NavHostFragment.findNavController(this);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
