package com.mt5dashboard.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.mt5dashboard.MT5DashboardApplication;
import com.mt5dashboard.core.MetaTraderSignalParser;
import com.mt5dashboard.core.RawParsedSignal;
import com.mt5dashboard.core.Signal;
import com.mt5dashboard.core.SignalParser;
import com.mt5dashboard.core.SignalValidator;

/**
 * دریافت اعلان‌ها منحصراً از اپلیکیشن رسمی MetaTrader 5 اندروید.
 *
 * مسیر پردازش (بخش ۲۶ سند اصلی):
 *   Notification -> این سرویس -> MetaTraderSignalParser -> SignalValidator -> SignalStateManager
 *
 * این سرویس عمداً بسیار سبک نگه داشته شده: هیچ منطق Scenario/Trigger/Alarm اینجا نیست
 * (اصل ۲۴: استقلال لایه‌ها). فقط وظیفه‌اش فیلتر کردن منبع + تحویل متن خام به Parser است.
 *
 * نکته فعال‌سازی: کاربر باید دستی این سرویس را در
 * Settings > Apps > Special access > Notification access
 * فعال کند (طبق بخش ۲ سند اصلی - این کار خودکار نیست و نمی‌تواند باشد).
 */
public class MT5NotificationListenerService extends NotificationListenerService {

    private static final String TAG = "MT5ListenerService";

    /** Package Name رسمی اپلیکیشن MetaTrader 5 اندروید (تأیید شده توسط کاربر). */
    private static final String MT5_PACKAGE_NAME = "net.metaquotes.metatrader5";

    private final SignalParser parser = new MetaTraderSignalParser();
    private final SignalValidator validator = new SignalValidator();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getPackageName() == null) return;
        if (!MT5_PACKAGE_NAME.equals(sbn.getPackageName())) {
            return; // اعلان از منبع دیگری است، نادیده گرفته می‌شود
        }

        String text = extractNotificationText(sbn);
        if (text == null) return;

        RawParsedSignal raw = parser.tryParse(text);
        if (raw == null) {
            // متن با فرمت شناخته‌شده سیگنال مطابقت ندارد (ممکن است اعلان دیگری از خود MT5 باشد)
            return;
        }

        long receivedAt = System.currentTimeMillis(); // همیشه ساعت دستگاه، هرگز Timestamp داخل متن (بخش ۳ و ۲۵ سند)
        SignalValidator.Result result = validator.validate(raw, receivedAt);

        if (!result.isValid()) {
            Log.w(TAG, "سیگنال رد شد: " + result.rejectionReason + " | متن خام: " + text);
            return;
        }

        Signal signal = result.signal;
        SignalStateManagerAccessor.get().upsertSignal(signal);
        Log.d(TAG, "سیگنال ثبت شد: " + signal);
    }

    private String extractNotificationText(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (notification == null) return null;
        Bundle extras = notification.extras;
        if (extras == null) return null;

        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (text != null) return text.toString();

        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (bigText != null) return bigText.toString();

        return null;
    }

    /** دسترسی ساده به Singleton مشترک، بدون وابستگی مستقیم به Application در امضای متدها. */
    private static final class SignalStateManagerAccessor {
        static com.mt5dashboard.core.SignalStateManager get() {
            return MT5DashboardApplication.getInstance().getSignalStateManager();
        }
    }
}
