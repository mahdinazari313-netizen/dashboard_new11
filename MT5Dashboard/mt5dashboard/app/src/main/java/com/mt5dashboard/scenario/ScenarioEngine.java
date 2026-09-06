package com.mt5dashboard.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mt5dashboard.core.Direction;
import com.mt5dashboard.core.Signal;
import com.mt5dashboard.core.SignalStateManager;

/**
 * یک نمونه مستقل موتور تصمیم‌گیری برای یک Scenario مشخص (اصل ۲۴ سند: هیچ
 * Engine ای از Engine دیگر خبر ندارد و هیچ State مشترکی بین آن‌ها نیست).
 *
 * دو مسیر کاملاً متفاوت ورودی دارد که عمداً هرگز نباید با هم قاطی شوند:
 *
 *   1) onNewSignal(signal) - رویداد "سیگنال خام جدید رسید" (بخش ۱۳ سند).
 *      همیشه یک ارزیابی کامل انجام می‌دهد و اگر شروط پاس شود، همیشه یک
 *      Trigger جدید می‌سازد (حتی اگر ترکیب قبلی هم پاس بود) - این رفتار
 *      عمداً Silent قبلی را می‌شکند (بخش ۱۴ سند: دریافت جدید = رویداد جدید).
 *
 *   2) periodicSafetyCheck(now) - فقط برای:
 *        الف) پاک‌سازی Triggerهایی که یکی از سیگنال‌هایشان منقضی شده (بخش ۱۵/۱۷)
 *        ب)  تکرار آلارم طبق Repeat Interval برای Trigger هنوز فعال و Silent-نشده (بخش ۲۲)
 *      این مسیر هرگز Trigger جدید نمی‌سازد و هرگز Silent را نمی‌شکند،
 *      چون صرفاً گذر زمان است، نه یک رویداد سیگنال جدید.
 */
public class ScenarioEngine {

    private final ScenarioConfig config;
    private final SignalStateManager signalStateManager;
    private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();
    private final List<AlarmListener> alarmListeners = new CopyOnWriteArrayList<>();

    /** آخرین Trigger فعال برای هر Symbol+Direction. کلید فقط وقتی وجود دارد که ترکیب فعلاً معتبر و پاس‌شده باشد. */
    private final Map<SymbolDirectionKey, Trigger> activeTriggers = new HashMap<>();

    public ScenarioEngine(ScenarioConfig config, SignalStateManager signalStateManager) {
        this.config = config;
        this.signalStateManager = signalStateManager;
    }

    public ScenarioConfig getConfig() {
        return config;
    }

    public void addAlarmListener(AlarmListener listener) {
        alarmListeners.add(listener);
    }

    public void removeAlarmListener(AlarmListener listener) {
        alarmListeners.remove(listener);
    }

    /** فقط Triggerهای فعلاً فعال (برای نمایش در Scenario Board - بخش ۲۰ و ۲۱ سند). */
    public List<Trigger> getActiveTriggers() {
        return new ArrayList<>(activeTriggers.values());
    }

    // =====================================================================
    // مسیر ۱: رویداد سیگنال خام جدید
    // =====================================================================

    /**
     * باید هر بار که یک سیگنال جدید (از هر Symbol/Timeframe/Direction) وارد
     * SignalStateManager می‌شود صدا زده شود. این متد فقط برای Symbol+Direction
     * همان سیگنال وارد شده ارزیابی مجدد انجام می‌دهد (بقیه Symbolها دست‌نخورده می‌مانند).
     */
    public void onNewSignal(Signal incomingSignal, long now) {
        if (!config.isEnabled()) return;

        SymbolDirectionKey key = new SymbolDirectionKey(incomingSignal.getSymbol(), incomingSignal.getDirection());
        List<Signal> validCombination = getValidCombination(key, now);

        if (validCombination.isEmpty()) {
            // هیچ سیگنال معتبری نمانده - اگر Trigger فعالی بود، دیگر معنی ندارد
            activeTriggers.remove(key);
            return;
        }

        ConditionResult result = conditionEvaluator.evaluateAll(validCombination, config);
        if (!result.passed) {
            // شروط پاس نشد - هر Trigger قبلی هم دیگر معتبر نیست
            activeTriggers.remove(key);
            return;
        }

        // شروط پاس شد -> طبق بخش ۱۳/۱۴ سند، این همیشه یک Trigger جدید است
        // (چه Trigger قبلی وجود داشته باشد چه نه)، و Silent قبلی را می‌شکند.
        Trigger newTrigger = new Trigger(config.getId(), key.getSymbol(), key.getDirection(), validCombination, now);
        activeTriggers.put(key, newTrigger);
        fireNewTrigger(newTrigger);
    }

    // =====================================================================
    // مسیر ۲: بررسی دوره‌ای امن (فقط انقضا + تکرار آلارم)
    // =====================================================================

    /**
     * باید به‌صورت دوره‌ای (مثلاً هر ۳۰-۶۰ ثانیه، یا با AlarmManager دقیق‌تر بر اساس
     * نزدیک‌ترین validUntil) صدا زده شود. هرگز Trigger جدید نمی‌سازد.
     */
    public void periodicSafetyCheck(long now) {
        if (!config.isEnabled()) return;

        List<SymbolDirectionKey> keysToRemove = new ArrayList<>();

        for (Map.Entry<SymbolDirectionKey, Trigger> entry : activeTriggers.entrySet()) {
            SymbolDirectionKey key = entry.getKey();
            Trigger trigger = entry.getValue();

            List<Signal> stillValid = getValidCombination(key, now);

            // آیا دقیقاً همان مجموعه سیگنال‌های قبلی هنوز معتبرند؟
            boolean sameSignalsStillValid = sameSignalSet(trigger, stillValid);

            if (stillValid.isEmpty() || !sameSignalsStillValid) {
                // یکی از اجزای ترکیب منقضی شده (بخش ۱۵/۱۷ سند) - Trigger از بین می‌رود.
                // توجه: این خودش Trigger جدید نمی‌سازد؛ اگر بعداً سیگنال جدیدی برسد،
                // onNewSignal دوباره از صفر ارزیابی می‌کند.
                keysToRemove.add(key);
                fireTriggerExpired(trigger);
                continue;
            }

            // ترکیب دقیقاً همان قبلی است و هنوز معتبر - فقط بررسی Repeat Interval
            if (!trigger.isSilenced()) {
                long repeatIntervalMillis = config.getRepeatIntervalMinutes() * 60_000L;
                if (now - trigger.getLastAlarmAt() >= repeatIntervalMillis) {
                    trigger.markAlarmFired(now);
                    fireRepeatAlarm(trigger);
                }
            }
        }

        for (SymbolDirectionKey key : keysToRemove) {
            activeTriggers.remove(key);
        }
    }

    // =====================================================================
    // Silent / Close
    // =====================================================================

    /** بخش ۱۴ سند: Silent فقط همین Trigger فعلی همان Symbol+Direction را ساکت می‌کند. */
    public void silence(String symbol, Direction direction) {
        SymbolDirectionKey key = new SymbolDirectionKey(symbol, direction);
        Trigger trigger = activeTriggers.get(key);
        if (trigger != null) {
            trigger.silence();
        }
    }

    // =====================================================================
    // کمکی‌ها
    // =====================================================================

    private List<Signal> getValidCombination(SymbolDirectionKey key, long now) {
        List<Signal> all = signalStateManager.getSignalsForSymbolAndDirection(key.getSymbol(), key.getDirection());
        List<Signal> valid = new ArrayList<>();
        for (Signal s : all) {
            if (s.isValidForScenario(now, config.getValidityMultiplier())) {
                valid.add(s);
            }
        }
        return valid;
    }

    private boolean sameSignalSet(Trigger trigger, List<Signal> currentValid) {
        if (trigger.getCombination().size() != currentValid.size()) return false;
        java.util.Set<String> currentIds = new java.util.HashSet<>();
        for (Signal s : currentValid) {
            currentIds.add(s.getId());
        }
        return currentIds.equals(trigger.getSignalIdSet());
    }

    private void fireNewTrigger(Trigger trigger) {
        for (AlarmListener listener : alarmListeners) {
            listener.onNewTrigger(config, trigger);
        }
    }

    private void fireRepeatAlarm(Trigger trigger) {
        for (AlarmListener listener : alarmListeners) {
            listener.onRepeatAlarm(config, trigger);
        }
    }

    private void fireTriggerExpired(Trigger trigger) {
        for (AlarmListener listener : alarmListeners) {
            listener.onTriggerExpired(config, trigger);
        }
    }
}
