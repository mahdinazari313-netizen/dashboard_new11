package com.mt5dashboard;

import android.app.Application;

import com.mt5dashboard.core.SignalStateManager;
import com.mt5dashboard.scenario.ScenarioEngineManager;

/**
 * نقطه‌ی اشتراک وضعیت بین NotificationListenerService (که سیگنال دریافت می‌کند)
 * و UI (که سیگنال‌ها/سناریوها را نمایش می‌دهد). فقط یک نمونه از هرکدام در کل عمر اپ.
 *
 * توجه: این کلاس عمداً فقط یک Container ساده است و هیچ منطق کسب‌وکاری ندارد
 * (اصل استقلال لایه‌ها - بخش ۲۴ سند اصلی).
 */
public class MT5DashboardApplication extends Application {

    private static MT5DashboardApplication instance;
    private final SignalStateManager signalStateManager = new SignalStateManager();
    private ScenarioEngineManager scenarioEngineManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // ScenarioEngineManager باید بعد از SignalStateManager ساخته شود چون در سازنده‌اش
        // به‌عنوان Listener روی آن ثبت‌نام می‌کند.
        scenarioEngineManager = new ScenarioEngineManager(signalStateManager);
    }

    public static MT5DashboardApplication getInstance() {
        return instance;
    }

    public SignalStateManager getSignalStateManager() {
        return signalStateManager;
    }

    public ScenarioEngineManager getScenarioEngineManager() {
        return scenarioEngineManager;
    }
}
