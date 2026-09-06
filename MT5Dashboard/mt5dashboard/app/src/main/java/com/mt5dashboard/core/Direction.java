package com.mt5dashboard.core;

/**
 * جهت سیگنال. طبق بخش ۴ سند فقط دو مقدار مجاز است.
 * این دو مقدار هرگز نباید بدون دستور صریح طراح ادغام یا حذف شوند (بخش ۲۵).
 */
public enum Direction {
    BUY,
    SELL;

    /**
     * پارس از متن خام Notification. مثال‌ها: "Buy Signal", "Sell Signal".
     * فقط کلمه Buy/Sell (بدون حساسیت به بزرگی حروف) تشخیص داده می‌شود.
     * @return Direction معتبر یا null در صورت عدم تطابق.
     */
    public static Direction fromRawText(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toUpperCase();
        if (normalized.contains("BUY")) return BUY;
        if (normalized.contains("SELL")) return SELL;
        return null;
    }
}
