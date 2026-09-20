package com.mt5dashboard.scenario;

import java.util.UUID;

import com.mt5dashboard.core.Timeframe;

/**
 * تنظیمات یک Scenario.
 *
 * تصمیم نهایی معماری (تأیید کاربر):
 *   Scenario یک "قانون کلی" است، نه مخصوص یک Symbol یا Direction خاص.
 *   هر Symbol که ترکیب مناسب نشان دهد، خودکار وارد ارزیابی این Scenario می‌شود
 *   (بخش ۱۲ سند: «اگر یک Symbol همزمان شرایط چند سناریو را داشته باشد...»).
 *   به همین قیاس، Scenario مستقل روی BUY و SELL هر Symbol اجرا می‌شود؛
 *   Symbol و Direction مشخص، خروجی هر Trigger هستند نه ورودی Scenario
 *   (Trigger شامل symbol+direction است - توافق قبلی).
 *
 * طبق تصمیم صریح کاربر: تمام تنظیمات و محدودیت‌ها داخل Scenario نگهداری می‌شوند.
 * بنابراین Minimum Timeframe Sync نیز بخشی از ScenarioConfig است.
 *
 * تصمیم بازنگری‌شده کاربر: یک شرط اختیاری برای حداقل تعداد تایم‌فریم هم‌زمان
 * در خود Scenario اضافه شده است. این شرط مستقل از Main Time Frame و
 * Price Difference است و فقط وقتی فعال باشد اعمال می‌شود.
 */
public class ScenarioConfig {

    private final String id;
    private String name;
    private boolean enabled = true; // پس از ساخت، خودکار فعال (بخش ۱۲ سند)

    private int validityMultiplier;      // بخش ۸ سند
    private boolean mainTimeFrameEnabled;
    private Timeframe mainTimeframe;     // فقط وقتی mainTimeFrameEnabled=true معنی دارد
    private boolean priceDifferenceEnabled; // بخش ۱۱ سند
    private int repeatIntervalMinutes;      // بخش ۱۱ و ۲۲ سند

    // شرط جدید: حداقل تعداد تایم‌فریم هم‌زمان
    private boolean minimumTimeframeSyncEnabled;
    private int minimumTimeframeCount = 2; // حداقل مجاز ۲

    public ScenarioConfig(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.validityMultiplier = 10; // مقدار پیش‌فرض منطقی، توسط کاربر قابل تغییر در UI
        this.repeatIntervalMinutes = 10;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getValidityMultiplier() {
        return validityMultiplier;
    }

    public void setValidityMultiplier(int validityMultiplier) {
        if (validityMultiplier <= 0) {
            throw new IllegalArgumentException("Signal Validity Multiplier باید مثبت باشد");
        }
        this.validityMultiplier = validityMultiplier;
    }

    public boolean isMainTimeFrameEnabled() {
        return mainTimeFrameEnabled;
    }

    public void setMainTimeFrameEnabled(boolean mainTimeFrameEnabled) {
        this.mainTimeFrameEnabled = mainTimeFrameEnabled;
    }

    public Timeframe getMainTimeframe() {
        return mainTimeframe;
    }

    public void setMainTimeframe(Timeframe mainTimeframe) {
        this.mainTimeframe = mainTimeframe;
    }

    public boolean isPriceDifferenceEnabled() {
        return priceDifferenceEnabled;
    }

    public void setPriceDifferenceEnabled(boolean priceDifferenceEnabled) {
        this.priceDifferenceEnabled = priceDifferenceEnabled;
    }

    public int getRepeatIntervalMinutes() {
        return repeatIntervalMinutes;
    }

    public void setRepeatIntervalMinutes(int repeatIntervalMinutes) {
        if (repeatIntervalMinutes <= 0) {
            throw new IllegalArgumentException("Signal Repeat Interval باید مثبت باشد");
        }
        this.repeatIntervalMinutes = repeatIntervalMinutes;
    }

    public boolean isMinimumTimeframeSyncEnabled() {
        return minimumTimeframeSyncEnabled;
    }

    public void setMinimumTimeframeSyncEnabled(boolean minimumTimeframeSyncEnabled) {
        this.minimumTimeframeSyncEnabled = minimumTimeframeSyncEnabled;
    }

    public int getMinimumTimeframeCount() {
        return minimumTimeframeCount;
    }

    public void setMinimumTimeframeCount(int minimumTimeframeCount) {
        if (minimumTimeframeCount < 2) {
            throw new IllegalArgumentException("Minimum Timeframe Sync باید حداقل ۲ باشد");
        }
        this.minimumTimeframeCount = minimumTimeframeCount;
    }
}
