package com.mt5dashboard.core;

/**
 * 21 تایم‌فریم معتبر پروژه.
 * baseMinutes = مدت پایه هر تایم‌فریم بر حسب دقیقه، برای محاسبه اعتبار زمانی
 * (بخش ۸ سند: مدت اعتبار = مدت پایه × Signal Validity Multiplier).
 *
 * توجه: PERIOD_CURRENT به‌عمد اینجا وجود ندارد (بخش ۴ سند).
 */
public enum Timeframe {
    M1(1),
    M2(2),
    M3(3),
    M4(4),
    M5(5),
    M6(6),
    M10(10),
    M12(12),
    M15(15),
    M20(20),
    M30(30),
    H1(60),
    H2(120),
    H3(180),
    H4(240),
    H6(360),
    H8(480),
    H12(720),
    D1(1440),
    W1(10080),
    MN1(43200); // تقریب ماه = 30 روز، در صورت نیاز به دقت تقویمی واقعی باید جداگانه بحث شود

    private final long baseMinutes;

    Timeframe(long baseMinutes) {
        this.baseMinutes = baseMinutes;
    }

    public long getBaseMinutes() {
        return baseMinutes;
    }

    /**
     * تبدیل رشته (مثلاً از متن Notification) به Timeframe معتبر.
     * @return Timeframe متناظر یا null اگر معتبر نبود (هرگز Exception پرتاب نمی‌کند
     *         تا SignalValidator بتواند تصمیم بگیرد پیام رد شود یا نه).
     */
    public static Timeframe fromString(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toUpperCase();
        for (Timeframe tf : values()) {
            if (tf.name().equals(normalized)) {
                return tf;
            }
        }
        return null;
    }
}
