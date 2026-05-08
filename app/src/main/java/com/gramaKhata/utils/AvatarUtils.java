package com.gramaKhata.utils;

import android.graphics.Color;
import android.text.TextUtils;

import java.util.Locale;

public final class AvatarUtils {

    private static final int[] AVATAR_COLORS = new int[]{
            Color.parseColor("#8D6E63"),
            Color.parseColor("#A1887F"),
            Color.parseColor("#6D4C41"),
            Color.parseColor("#D4A373"),
            Color.parseColor("#BC6C25"),
            Color.parseColor("#9C6644"),
            Color.parseColor("#7F5539"),
            Color.parseColor("#B08968")
    };

    private AvatarUtils() {
    }

    public static int getColorForName(String name) {
        int hash = name == null ? 0 : name.trim().hashCode();
        int index = (hash & Integer.MAX_VALUE) % AVATAR_COLORS.length;
        return AVATAR_COLORS[index];
    }

    public static String getInitial(String name) {
        if (TextUtils.isEmpty(name)) {
            return "?";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
