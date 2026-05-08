package com.gramaKhata.utils;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private static final Locale INR_LOCALE = new Locale("en", "IN");

    private CurrencyFormatter() {
    }

    public static String format(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(INR_LOCALE);
        return currencyFormat.format(amount);
    }

    public static String formatAbs(double amount) {
        return format(Math.abs(amount));
    }
}
