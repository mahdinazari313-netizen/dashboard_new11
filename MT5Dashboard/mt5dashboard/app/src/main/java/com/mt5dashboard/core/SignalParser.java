package com.mt5dashboard.core;

/**
 * قرارداد لایه Parser. اگر در آینده فرمت Notification MT5 تغییر کند،
 * کافی است پیاده‌سازی جدیدی از این Interface نوشته شود؛
 * بقیه سیستم (Validator، StateManager، ScenarioEngine) دست‌نخورده می‌ماند.
 */
public interface SignalParser {
    /**
     * تلاش برای استخراج داده خام از متن Notification.
     * @param notificationText متن کامل اعلان دریافتی
     * @return RawParsedSignal در صورت تطابق فرمت، یا null اگر متن با فرمت شناخته‌شده مطابقت نداشت
     *         (هرگز Exception پرتاب نمی‌کند - عدم تطابق فرمت یک حالت عادی است،
     *          چون NotificationListener ممکن است پیام‌های نامرتبط دیگری هم از همان اپ دریافت کند).
     */
    RawParsedSignal tryParse(String notificationText);
}
