package com.mt5dashboard.alarm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mt5dashboard.MT5DashboardApplication;
import com.mt5dashboard.core.Direction;
import com.mt5dashboard.scenario.ScenarioEngine;

/**
 * وقتی کاربر دکمه Silent را روی نوتیفیکیشن آلارم می‌زند، این Receiver صدا زده می‌شود.
 * فقط همان Trigger فعلی (Symbol+Direction داخل همان Scenario) را Silent می‌کند
 * (بخش ۱۴ سند: Silent فقط مربوط به Trigger فعلی است، نه کل Scenario).
 */
public class SilenceActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!AlarmNotifier.ACTION_SILENCE.equals(intent.getAction())) return;

        String scenarioId = intent.getStringExtra(AlarmNotifier.EXTRA_SCENARIO_ID);
        String symbol = intent.getStringExtra(AlarmNotifier.EXTRA_SYMBOL);
        String directionRaw = intent.getStringExtra(AlarmNotifier.EXTRA_DIRECTION);
        if (scenarioId == null || symbol == null || directionRaw == null) return;

        Direction direction;
        try {
            direction = Direction.valueOf(directionRaw);
        } catch (IllegalArgumentException e) {
            return;
        }

        ScenarioEngine engine = MT5DashboardApplication.getInstance()
                .getScenarioEngineManager()
                .getEngine(scenarioId);
        if (engine != null) {
            engine.silence(symbol, direction);
        }

        // نوتیفیکیشن فعلی را ببند چون تا رویداد جدید یا Repeat دیگری در کار نیست
        int notificationId = (scenarioId + "|" + symbol + "|" + directionRaw).hashCode();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId);
        }
    }
}
