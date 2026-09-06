package com.mt5dashboard.scenario;

/**
 * ScenarioEngine با این Interface به بیرون (UI، صدا، نوتیفیکیشن) اطلاع می‌دهد.
 * خودِ Engine هیچ ایده‌ای درباره‌ی نحوه‌ی نمایش/پخش آلارم ندارد (استقلال لایه‌ها).
 */
public interface AlarmListener {

    /** یک Trigger کاملاً جدید صادر شد (بخش ۱۳ سند) - همیشه باید به کاربر اطلاع داده شود. */
    void onNewTrigger(ScenarioConfig scenario, Trigger trigger);

    /** همان Trigger فعلی، طبق Repeat Interval دوباره آلارم داد (بخش ۲۲ سند). */
    void onRepeatAlarm(ScenarioConfig scenario, Trigger trigger);

    /** یک Trigger فعال (به‌خاطر انقضای یکی از سیگنال‌هایش) از بین رفت (بخش ۱۵ و ۱۷ سند). */
    void onTriggerExpired(ScenarioConfig scenario, Trigger trigger);
}
