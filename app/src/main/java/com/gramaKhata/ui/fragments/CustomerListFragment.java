package com.gramaKhata.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.gramaKhata.R;
import com.gramaKhata.data.db.CustomerDao;
import com.gramaKhata.data.db.CustomerEntity;
import com.gramaKhata.databinding.DialogShopNameBinding;
import com.gramaKhata.databinding.FragmentCustomerListBinding;
import com.gramaKhata.ui.adapter.CustomerAdapter;
import com.gramaKhata.ui.viewmodel.CustomerListViewModel;
import com.gramaKhata.utils.CurrencyFormatter;
import com.gramaKhata.utils.PreferencesManager;

import java.util.Collections;
import java.util.List;

public class CustomerListFragment extends Fragment {

    private FragmentCustomerListBinding binding;
    private CustomerListViewModel viewModel;
    private CustomerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentCustomerListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CustomerListViewModel.class);

        setupToolbar();
        setupList();
        setupSearch();
        setupTimeframeSpinner();
        setupActions();
        observeData();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToolbarSubtitle();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_customer_list);
        updateToolbarSubtitle();
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                showUpdateShopNameDialog();
                return true;
            }
            return false;
        });
    }

    private void updateToolbarSubtitle() {
        String shopName = PreferencesManager.getShopName(requireContext());
        if (shopName != null && !shopName.trim().isEmpty()) {
            binding.toolbar.setSubtitle(shopName);
        } else {
            binding.toolbar.setSubtitle(null);
        }
    }

    private void showUpdateShopNameDialog() {
        DialogShopNameBinding dialogBinding = DialogShopNameBinding.inflate(
                LayoutInflater.from(requireContext())
        );

        // Pre-fill with current shop name
        String currentName = PreferencesManager.getShopName(requireContext());
        if (currentName != null && !currentName.trim().isEmpty()) {
            dialogBinding.etShopName.setText(currentName);
            dialogBinding.etShopName.setSelection(currentName.length());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.update_shop_name_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String shopName = dialogBinding.etShopName.getText() == null
                            ? ""
                            : dialogBinding.etShopName.getText().toString().trim();
                    if (shopName.isEmpty()) {
                        dialogBinding.etShopName.setError(getString(R.string.shop_name_required));
                        return;
                    }
                    PreferencesManager.saveShopName(requireContext(), shopName);
                    updateToolbarSubtitle();
                    Snackbar.make(binding.getRoot(), R.string.shop_name_updated, Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void setupList() {
        adapter = new CustomerAdapter(
                Collections.emptyList(),
                this::openCustomerDetail,
                this::confirmDeleteCustomer
        );
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCustomers.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setSearchQuery(editable == null ? "" : editable.toString());
            }
        });
    }

    private void setupTimeframeSpinner() {
        String[] options = {"Today", "Yesterday", "Day before yesterday", "All days"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, options);
        binding.spinnerTimeframe.setAdapter(spinnerAdapter);
        binding.spinnerTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CustomerListViewModel.Timeframe timeframe = CustomerListViewModel.Timeframe.TODAY;
                if (position == 1) timeframe = CustomerListViewModel.Timeframe.YESTERDAY;
                else if (position == 2) timeframe = CustomerListViewModel.Timeframe.DAY_BEFORE_YESTERDAY;
                else if (position == 3) timeframe = CustomerListViewModel.Timeframe.ALL_TIME;
                viewModel.setTimeframeFilter(timeframe);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupActions() {
        binding.fabAdd.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("customerId", -1);
            findNavController().navigate(R.id.action_to_add_customer, args);
        });
    }

    private void observeData() {
        viewModel.getFilteredCustomers().observe(getViewLifecycleOwner(), customers -> {
            List<CustomerDao.CustomerWithBalance> safeList =
                    customers == null ? Collections.emptyList() : customers;
            adapter.updateList(safeList);
            boolean isEmpty = safeList.isEmpty();
            binding.llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvCustomers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getTotalOutstanding().observe(getViewLifecycleOwner(), amount ->
                binding.tvTotalBalance.setText(CurrencyFormatter.format(amount == null ? 0d : amount))
        );
    }

    private void openCustomerDetail(CustomerDao.CustomerWithBalance customer) {
        if (customer == null || customer.id <= 0) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("customerId", customer.id);
        findNavController().navigate(R.id.action_to_detail, args);
    }

    private void confirmDeleteCustomer(CustomerDao.CustomerWithBalance customer) {
        if (customer == null) {
            return;
        }
        String customerName = customer.name == null ? "" : customer.name.trim();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_customer_title, customerName))
                .setMessage(R.string.delete_customer_transactions_warning)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_customer, (dialog, which) -> {
                    CustomerEntity entity = new CustomerEntity();
                    entity.setId(customer.id);
                    entity.setName(customer.name);
                    entity.setPhone(customer.phone);
                    entity.setPhotoUri(customer.photoUri);
                    entity.setCreatedAt(System.currentTimeMillis());
                    viewModel.deleteCustomer(entity);
                })
                .show();
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

