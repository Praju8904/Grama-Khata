package com.gramaKhata.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
    }

    public static String formatDate(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }

    public static boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTimeInMillis(timestamp);
        return today.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR);
    }

    public static String todayFormatted() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
    }
}
