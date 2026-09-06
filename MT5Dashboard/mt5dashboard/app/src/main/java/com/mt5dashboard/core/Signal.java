package com.mt5dashboard.core;

import java.util.Objects;
import java.util.UUID;

/**
 * یک سیگنال خام دریافت‌شده از MT5، پس از Parse و Validate.
 *
 * تصمیم نهایی معماری (اصلاح‌شده پس از بحث):
 *   Signal فقط داده خام + زمان دریافت را نگه می‌دارد.
 *   هیچ مفهوم "انقضا"یی داخل خود Signal ذخیره نمی‌شود، چون:
 *
 *   1) اعتبار منطقی (Scenario Validity) کاملاً به تنظیمات هر Scenario وابسته است
 *      (Signal Validity Multiplier مال Scenario است، نه Signal - بخش ۸ سند).
 *      بنابراین محاسبه می‌شود، نه ذخیره: هر ScenarioEngine خودش با فرمول
 *          receivedAt + timeframe.baseMinutes × scenario.validityMultiplier
 *      اعتبار را در لحظه ارزیابی حساب می‌کند. یک Signal واحد می‌تواند هم‌زمان
 *      برای یک Scenario معتبر و برای Scenario دیگر منقضی باشد.
 *
 *   2) نمایش در صفحه اول (Display Duration) کاملاً مستقل و متعلق به خود صفحه اول است
 *      (بخش ۱۸/۱۹ سند؛ تأیید صریح کاربر: «پنجره نمایش... اینپوت‌ها را جدا و کامل
 *      در خودش دارد... عملکردش کاملاً جدا از سناریوست»). بنابراین Signal حتی
 *      displayUntil را هم نگه نمی‌دارد؛ خود لایه UI صفحه اول با
 *          receivedAt + page1Settings.displayDurationMinutes
 *      تصمیم می‌گیرد چه چیزی نمایش داده شود. این دو محاسبه کاملاً بی‌ربط از هم
 *      روی همین یک receivedAt مشترک انجام می‌شوند و هرگز نباید در یک فیلد ادغام شوند
 *      (بخش ۲۵، قانون جلوگیری از خطا).
 */
public final class Signal {

    private final String id; // شناسه داخلی یکتا، برای ردیابی دقیق در Trigger و Log
    private final String symbol;
    private final Timeframe timeframe;
    private final Direction direction;
    private final double price;
    private final long receivedAt; // epoch millis, بر اساس System.currentTimeMillis() دستگاه Android

    public Signal(String symbol, Timeframe timeframe, Direction direction, double price, long receivedAt) {
        this.id = UUID.randomUUID().toString();
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.direction = direction;
        this.price = price;
        this.receivedAt = receivedAt;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getPrice() {
        return price;
    }

    public long getReceivedAt() {
        return receivedAt;
    }

    /**
     * آیا این سیگنال، طبق تنظیمات یک Scenario مشخص (validityMultiplier آن)،
     * در این لحظه هنوز معتبر است. این مقدار ثابت نیست و بسته به Scenario فرق می‌کند
     * (بخش ۸ سند). محاسبه، نه ذخیره.
     */
    public boolean isValidForScenario(long nowMillis, int validityMultiplier) {
        long validUntil = receivedAt + timeframe.getBaseMinutes() * 60_000L * validityMultiplier;
        return nowMillis < validUntil;
    }

    /**
     * لحظه دقیق انقضای این سیگنال برای یک Scenario مشخص. برای زمان‌بندی
     * Event-driven Expiration (Alarm/Timer) کاربرد دارد.
     */
    public long getValidUntilForScenario(int validityMultiplier) {
        return receivedAt + timeframe.getBaseMinutes() * 60_000L * validityMultiplier;
    }

    /**
     * کلید هویتی این سیگنال طبق تصمیم نهایی معماری: Symbol + Timeframe + Direction.
     * دو سیگنال با این کلید یکسان، وضعیت یکسانی در SignalStateManager محسوب می‌شوند
     * و دومی جایگزین اولی خواهد شد.
     */
    public SignalKey getKey() {
        return new SignalKey(symbol, timeframe, direction);
    }

    /**
     * مقایسه قیمت دقیق، بدون هیچ Threshold (بخش ۷ سند).
     * این متد صرفاً یک Wrapper خواناست؛ منطق واقعی همان مقایسه == اعداد double است.
     */
    public boolean hasSamePrice(Signal other) {
        if (other == null) return false;
        return Double.compare(this.price, other.price) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Signal)) return false;
        Signal signal = (Signal) o;
        return id.equals(signal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Signal{" +
                "symbol='" + symbol + '\'' +
                ", timeframe=" + timeframe +
                ", direction=" + direction +
                ", price=" + price +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
