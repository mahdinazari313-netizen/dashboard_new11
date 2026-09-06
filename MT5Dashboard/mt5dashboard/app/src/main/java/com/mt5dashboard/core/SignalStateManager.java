package com.mt5dashboard.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * نگهداری وضعیت فعلی همه سیگنال‌ها.
 *
 * Key = Symbol + Timeframe + Direction (طبق تصمیم نهایی معماری، بخش ۵ و ۶ سند اول).
 * دریافت سیگنال جدید با کلید یکسان -> جایگزینی کامل سیگنال قبلی (بخش ۶ سند).
 *
 * این کلاس عمداً هیچ منطق Scenario/Condition/Validity/Display ندارد
 * (اصل ۲۴: استقلال لایه‌ها + تصمیم نهایی: هیچ تنظیماتی بیرون از Scenario یا Page1
 * در این لایه اعمال نمی‌شود). فقط وظیفه‌اش نگهداری صادقانه «چه سیگنالی الان
 * چه داده‌ای دارد» است؛ تفسیر «معتبر است یا نه» کاملاً به عهده لایه‌های بالاتر
 * (ScenarioEngine برای اعتبار Scenario، Page1 UI برای Display Duration) است.
 *
 * نکته مهم درباره حافظه: چون کلید محدود است (تعداد نمادها × ۲۱ تایم‌فریم × ۲ جهت)،
 * و سیگنال جدید همیشه جای قدیمی را با همان کلید می‌گیرد، حجم این Map به‌طور طبیعی
 * محدود می‌ماند و نیازی به Purge دوره‌ای برای مدیریت حافظه نیست.
 *
 * Thread-safety: چون سیگنال از NotificationListenerService (Thread سیستم) می‌رسد
 * و ممکن است هم‌زمان از UI Thread یا ScenarioEngine خوانده شود، از ConcurrentHashMap
 * استفاده شده و Listenerها روی CopyOnWriteArrayList نگه داشته می‌شوند.
 */
public class SignalStateManager {

    /** برای اطلاع‌رسانی Event-driven به لایه‌های بالاتر (ScenarioEngineManager، Page1 UI). */
    public interface Listener {
        /** هر بار سیگنالی اضافه/جایگزین شود صدا زده می‌شود. */
        void onSignalStateChanged(Signal newOrUpdatedSignal);
    }

    private final Map<SignalKey, Signal> signals = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Signal signal) {
        for (Listener l : listeners) {
            l.onSignalStateChanged(signal);
        }
    }

    /**
     * دریافت سیگنال جدید. طبق بخش ۶ سند:
     *   - اگر کلید (Symbol+Timeframe+Direction) از قبل وجود داشته باشد، جایگزین می‌شود.
     *   - این یک رویداد جدید محسوب می‌شود، حتی اگر سیگنال قبلی هنوز معتبر بود.
     *   - جهت مقابل (BUY/SELL دیگر با همان Symbol+Timeframe) دست‌نخورده می‌ماند.
     */
    public void upsertSignal(Signal newSignal) {
        signals.put(newSignal.getKey(), newSignal);
        notifyListeners(newSignal);
    }

    /** حذف صریح یک سیگنال با کلید مشخص (مثلاً درخواست دستی کاربر). */
    public void removeSignal(SignalKey key) {
        signals.remove(key);
    }

    /** آخرین سیگنال ثبت‌شده برای یک کلید خاص، یا null. */
    public Signal getSignal(SignalKey key) {
        return signals.get(key);
    }

    /** تمام سیگنال‌های موجود (بدون فیلتر اعتبار - تفسیر اعتبار به عهده مصرف‌کننده است). */
    public List<Signal> getAllSignals() {
        return new ArrayList<>(signals.values());
    }

    /** تمام سیگنال‌های موجود برای یک Symbol خاص (کاربرد در Page1: هر Symbol یک Card). */
    public List<Signal> getSignalsForSymbol(String symbol) {
        List<Signal> result = new ArrayList<>();
        for (Signal s : signals.values()) {
            if (s.getSymbol().equals(symbol)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * تمام سیگنال‌های موجود برای یک Symbol+Direction خاص (کاربرد در ScenarioEngine
     * برای ساخت ترکیب). این متد صرفاً یک کوئری عمومی روی داده خام است؛
     * هیچ تفسیری از اعتبار Scenario انجام نمی‌دهد.
     */
    public List<Signal> getSignalsForSymbolAndDirection(String symbol, Direction direction) {
        List<Signal> result = new ArrayList<>();
        for (Signal s : signals.values()) {
            if (s.getSymbol().equals(symbol) && s.getDirection() == direction) {
                result.add(s);
            }
        }
        return result;
    }

    /** لیست یکتای نمادهایی که حداقل یک سیگنال ثبت‌شده دارند. */
    public List<String> getDistinctSymbols() {
        List<String> result = new ArrayList<>();
        for (Signal s : signals.values()) {
            if (!result.contains(s.getSymbol())) {
                result.add(s.getSymbol());
            }
        }
        return result;
    }

    /** برای تست/دیباگ: تعداد کل سیگنال‌های نگه‌داری‌شده. */
    public int size() {
        return signals.size();
    }
}
