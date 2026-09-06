package com.mt5dashboard.ui;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * نگهداری تنظیمات مخصوص صفحه اول (Active Signals).
 *
 * طبق تصمیم نهایی معماری: Display Duration کاملاً مستقل از هر Scenario است
 * و هرگز نباید با Signal Validity Multiplier (که مال Scenario است) یکی فرض شود.
 */
public class AppSettings {

    private static final String PREFS_NAME = "mt5dashboard_settings";
    private static final String KEY_DISPLAY_DURATION_MINUTES = "display_duration_minutes";
    private static final int DEFAULT_DISPLAY_DURATION_MINUTES = 60;

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getDisplayDurationMinutes() {
        return prefs.getInt(KEY_DISPLAY_DURATION_MINUTES, DEFAULT_DISPLAY_DURATION_MINUTES);
    }

    public void setDisplayDurationMinutes(int minutes) {
        prefs.edit().putInt(KEY_DISPLAY_DURATION_MINUTES, minutes).apply();
    }
}
