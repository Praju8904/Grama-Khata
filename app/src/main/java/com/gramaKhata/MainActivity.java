package com.gramaKhata;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.gramaKhata.databinding.ActivityMainBinding;
import com.gramaKhata.databinding.DialogShopNameBinding;
import com.gramaKhata.utils.PreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found");
        }
        navController = navHostFragment.getNavController();

        if (savedInstanceState == null && PreferencesManager.isFirstLaunch(this)) {
            showShopNameDialog();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void showShopNameDialog() {
        DialogShopNameBinding dialogBinding = DialogShopNameBinding.inflate(
                LayoutInflater.from(this)
        );

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
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
                    PreferencesManager.saveShopName(MainActivity.this, shopName);
                    PreferencesManager.setFirstLaunchDone(MainActivity.this);
                    dialog.dismiss();
                }));
        dialog.show();
    }
}
