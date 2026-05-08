package com.gramaKhata.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.gramaKhata.R;
import com.gramaKhata.databinding.FragmentAddEditCustomerBinding;
import com.gramaKhata.ui.viewmodel.AddEditCustomerViewModel;

public class AddEditCustomerFragment extends Fragment {

    private FragmentAddEditCustomerBinding binding;
    private AddEditCustomerViewModel viewModel;
    private int customerId = -1;
    private boolean isEditing;
    private boolean hasPopulatedName;
    private boolean hasPopulatedPhone;

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || binding == null) {
                    return;
                }
                viewModel.setPhotoUri(uri.toString());
                Glide.with(this).load(uri).circleCrop().into(binding.ivPhoto);
            });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAddEditCustomerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddEditCustomerViewModel.class);
        customerId = getArguments() == null ? -1 : getArguments().getInt("customerId", -1);
        isEditing = customerId != -1;

        setupToolbar();
        setupActions();
        observeViewModel();

        if (isEditing) {
            binding.btnDelete.setVisibility(View.VISIBLE);
            viewModel.loadCustomer(customerId);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setTitle(isEditing ? R.string.edit_customer : R.string.add_customer);
        binding.toolbar.setNavigationOnClickListener(v -> findNavController().popBackStack());
    }

    private void setupActions() {
        binding.ivPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> {
            viewModel.name.setValue(valueOf(binding.etName));
            viewModel.phone.setValue(valueOf(binding.etPhone));
            viewModel.saveCustomer(customerId);
        });
        binding.btnDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_customer_confirm_title)
                .setMessage(R.string.delete_customer_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_customer, (dialog, which) -> {
                    viewModel.deleteCustomer(customerId);
                    findNavController().popBackStack();
                })
                .show());
    }

    private void observeViewModel() {
        viewModel.name.observe(getViewLifecycleOwner(), name -> {
            if (!isEditing || hasPopulatedName || name == null) {
                return;
            }
            binding.etName.setText(name);
            hasPopulatedName = true;
        });

        viewModel.phone.observe(getViewLifecycleOwner(), phone -> {
            if (!isEditing || hasPopulatedPhone || phone == null) {
                return;
            }
            binding.etPhone.setText(phone);
            hasPopulatedPhone = true;
        });

        viewModel.photoUri.observe(getViewLifecycleOwner(), uri -> {
            if (uri == null || uri.trim().isEmpty()) {
                return;
            }
            Glide.with(this).load(Uri.parse(uri)).circleCrop().into(binding.ivPhoto);
        });

        viewModel.saveSuccess.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                findNavController().popBackStack();
            }
        });

        viewModel.validationError.observe(getViewLifecycleOwner(), error -> {
            if (TextUtils.isEmpty(error)) {
                return;
            }
            Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
        });
    }

    private String valueOf(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
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
