package com.mt5dashboard.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mt5dashboard.core.Signal;
import com.mt5dashboard.core.SignalStateManager;

/**
 * نگهدارنده تمام Scenario Engine های موجود در اپ.
 *
 * این کلاس فقط "دیسپچر" است: هر سیگنال جدید را به تمام Engineها می‌رساند
 * و هر کدام مستقل و بدون اطلاع از بقیه تصمیم می‌گیرند (اصل ۲۴ سند).
 * این کلاس خودش هیچ منطق Condition/Trigger ندارد.
 */
public class ScenarioEngineManager implements SignalStateManager.Listener {

    private final SignalStateManager signalStateManager;
    private final Map<String, ScenarioEngine> engines = new ConcurrentHashMap<>(); // key = scenarioId

    /** Listenerهایی که باید به تمام Engineها (فعلی و آینده) وصل شوند - مثلاً AlarmNotifier. */
    private final List<AlarmListener> globalAlarmListeners = new CopyOnWriteArrayList<>();

    public ScenarioEngineManager(SignalStateManager signalStateManager) {
        this.signalStateManager = signalStateManager;
        this.signalStateManager.addListener(this);
    }

    public void addGlobalAlarmListener(AlarmListener listener) {
        globalAlarmListeners.add(listener);
        for (ScenarioEngine engine : engines.values()) {
            engine.addAlarmListener(listener);
        }
    }

    /** ساخت سناریوی جدید: طبق بخش ۱۲ سند، بلافاصله و خودکار فعال می‌شود. */
    public ScenarioEngine createScenario(ScenarioConfig config) {
        ScenarioEngine engine = new ScenarioEngine(config, signalStateManager);
        for (AlarmListener listener : globalAlarmListeners) {
            engine.addAlarmListener(listener);
        }
        engines.put(config.getId(), engine);
        return engine;
    }

    /**
     * Close Scenario (بخش ۱۶ سند): کل سناریو حذف می‌شود، بدون نگهداری تاریخچه.
     * این با Silent (که فقط روی یک Trigger داخل ScenarioEngine اثر دارد) کاملاً متفاوت است.
     */
    public void closeScenario(String scenarioId) {
        engines.remove(scenarioId);
    }

    public ScenarioEngine getEngine(String scenarioId) {
        return engines.get(scenarioId);
    }

    public List<ScenarioEngine> getAllEngines() {
        return new ArrayList<>(engines.values());
    }

    // ---------------- دریافت رویداد از SignalStateManager ----------------

    @Override
    public void onSignalStateChanged(Signal newOrUpdatedSignal) {
        long now = System.currentTimeMillis();
        // به تمام Engineها اطلاع بده - هر کدام مستقل تصمیم می‌گیرد که آیا برایش مهم است یا نه
        for (ScenarioEngine engine : engines.values()) {
            engine.onNewSignal(newOrUpdatedSignal, now);
        }
    }

    // ---------------- بررسی دوره‌ای امن ----------------

    /**
     * باید به‌صورت دوره‌ای صدا زده شود (مثلاً هر ۳۰-۶۰ ثانیه از یک Foreground Service
     * یا AlarmManager). فقط انقضا و تکرار آلارم را مدیریت می‌کند؛ Trigger جدید نمی‌سازد.
     */
    public void runPeriodicSafetyCheck() {
        long now = System.currentTimeMillis();
        for (ScenarioEngine engine : engines.values()) {
            engine.periodicSafetyCheck(now);
        }
    }
}

