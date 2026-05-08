package com.gramaKhata.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class PreferencesManager {

    private static final String PREF_NAME = "grama_khata_preferences";
    private static final String KEY_SHOP_NAME = "shop_name";
    private static final String KEY_FIRST_LAUNCH = "is_first_launch";

    private PreferencesManager() {
    }

    public static void saveShopName(Context context, String shopName) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_SHOP_NAME, shopName == null ? "" : shopName).apply();
    }

    public static String getShopName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_SHOP_NAME, "");
    }

    public static boolean isFirstLaunch(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public static void setFirstLaunchDone(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
}
