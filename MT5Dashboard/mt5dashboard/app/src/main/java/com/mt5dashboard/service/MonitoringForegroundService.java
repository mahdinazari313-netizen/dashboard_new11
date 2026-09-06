package com.mt5dashboard.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mt5dashboard.MT5DashboardApplication;
import com.mt5dashboard.MainActivity;
import com.mt5dashboard.R;
import com.mt5dashboard.alarm.AlarmNotifier;

/**
 * سرویس سبک با دو وظیفه:
 *   ۱) نمایش نوتیفیکیشن دائمی وضعیت اجرا (هدف اصلی این کلاس).
 *   ۲) میزبانی Tick دوره‌ای Safety Check موتور سناریوها (چون این سرویس همیشه زنده است،
 *      بهترین جای موجود برای این Tick است؛ بدون نیاز به یک Service جداگانه دیگر).
 *
 * توجه معماری مهم: این سرویس هیچ منطق Signal/Scenario را خودش پیاده‌سازی نمی‌کند،
 * فقط زمان‌بند صدا زدن ScenarioEngineManager.runPeriodicSafetyCheck() است
 * (اصل ۲۴: استقلال لایه‌ها - این سرویس صرفاً یک Timer بیرونی است).
 */
public class MonitoringForegroundService extends Service {

    public static final String CHANNEL_ID = "mt5_monitoring_status_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long SAFETY_CHECK_INTERVAL_MILLIS = 60_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean alarmNotifierRegistered = false;

    private final Runnable safetyCheckTick = new Runnable() {
        @Override
        public void run() {
            MT5DashboardApplication.getInstance().getScenarioEngineManager().runPeriodicSafetyCheck();
            handler.postDelayed(this, SAFETY_CHECK_INTERVAL_MILLIS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannelIfNeeded();
        startForeground(NOTIFICATION_ID, buildNotification(true));
        registerAlarmNotifierIfNeeded();
        handler.postDelayed(safetyCheckTick, SAFETY_CHECK_INTERVAL_MILLIS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // اگر سرویس دوباره توسط سیستم راه‌اندازی شود، نوتیفیکیشن باید بلافاصله دوباره نمایش داده شود.
        startForeground(NOTIFICATION_ID, buildNotification(true));
        registerAlarmNotifierIfNeeded();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(safetyCheckTick);
    }

    /** ثبت AlarmNotifier فقط یک بار در کل عمر Process، صرف‌نظر از این‌که سرویس چند بار Restart شود. */
    private void registerAlarmNotifierIfNeeded() {
        if (alarmNotifierRegistered) return;
        MT5DashboardApplication.getInstance().getScenarioEngineManager()
                .addGlobalAlarmListener(new AlarmNotifier(this));
        alarmNotifierRegistered = true;
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        getString(R.string.monitoring_notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW // بدون صدا، بدون Heads-up - فقط یک نشانه ساکت
                );
                channel.setDescription(getString(R.string.monitoring_notification_channel_desc));
                channel.setShowBadge(false);
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(boolean running) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openAppIntent, pendingFlags);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_monitor_status)
                .setContentTitle(getString(R.string.monitoring_notification_title))
                .setContentText(running
                        ? getString(R.string.monitoring_notification_text_running)
                        : getString(R.string.monitoring_notification_text_stopped))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Bound service نیست، فقط Started+Foreground
    }
}
