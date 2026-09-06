package com.mt5dashboard.core;

/**
 * لایه اعتبارسنجی: خروجی خام (رشته‌ای) Parser را می‌گیرد و در صورت معتبر بودن،
 * یک Signal واقعی و Type-safe می‌سازد.
 *
 * بررسی‌ها (طبق بخش ۴ سند):
 *   - Symbol خالی نباشد.
 *   - Timeframe باید یکی از ۲۱ تایم‌فریم معتبر باشد (Timeframe.fromString).
 *   - Direction باید دقیقاً Buy یا Sell باشد (Direction.fromRawText).
 *   - Price باید یک عدد قابل‌پارس باشد (بدون هیچ Threshold یا گرد کردن - بخش ۷ سند).
 *
 * این کلاس عمداً هیچ منطق مربوط به اعتبار زمانی یا Scenario ندارد؛
 * فقط "آیا این پیام یک سیگنال قابل‌قبول است یا نه" را تعیین می‌کند.
 */
public class SignalValidator {

    /**
     * نتیجه اعتبارسنجی. یا signal معتبر برمی‌گرداند یا دلیل رد شدن را.
     */
    public static final class Result {
        public final Signal signal; // null اگر نامعتبر
        public final String rejectionReason; // null اگر معتبر

        private Result(Signal signal, String rejectionReason) {
            this.signal = signal;
            this.rejectionReason = rejectionReason;
        }

        public boolean isValid() {
            return signal != null;
        }

        static Result ok(Signal signal) {
            return new Result(signal, null);
        }

        static Result rejected(String reason) {
            return new Result(null, reason);
        }
    }

    /**
     * @param raw خروجی خام لایه Parser
     * @param receivedAtMillis زمان دریافت بر اساس ساعت دستگاه Android
     *                         (هرگز از Timestamp داخل متن MT5 - بخش ۳ و ۲۵ سند)
     */
    public Result validate(RawParsedSignal raw, long receivedAtMillis) {
        if (raw == null) {
            return Result.rejected("ورودی خام null است");
        }

        if (raw.symbol == null || raw.symbol.trim().isEmpty()) {
            return Result.rejected("Symbol خالی است");
        }

        Timeframe timeframe = Timeframe.fromString(raw.timeframe);
        if (timeframe == null) {
            return Result.rejected("Timeframe نامعتبر: " + raw.timeframe);
        }

        Direction direction = Direction.fromRawText(raw.direction);
        if (direction == null) {
            return Result.rejected("Direction نامعتبر: " + raw.direction);
        }

        double price;
        try {
            price = Double.parseDouble(raw.priceText);
        } catch (NumberFormatException | NullPointerException e) {
            return Result.rejected("Price نامعتبر: " + raw.priceText);
        }

        Signal signal = new Signal(raw.symbol.trim(), timeframe, direction, price, receivedAtMillis);
        return Result.ok(signal);
    }
}
