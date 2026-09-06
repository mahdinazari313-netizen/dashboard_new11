package com.mt5dashboard.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser مخصوص فرمت دقیق اعلان MT5 که کاربر تأیید کرد (کپی دقیق):
 *
 *   (ASXAUD, M12) Sell Signal-(M12-9198.5)-(2026.08.25 20:48:00]
 *
 * نکات مهم که عمداً در Regex لحاظ شده‌اند:
 *   - پرانتز پایانی نامتقارن است: بخش تاریخ/ساعت با ')' باز و با ']' بسته می‌شود.
 *     این تأیید شده که همیشه همین‌طور است، پس عیناً همین‌گونه Parse می‌شود
 *     (هیچ فرض تقارنی اعمال نشده).
 *   - Timestamp داخل متن (بخش تاریخ/ساعت) عمداً استخراج و استفاده نمی‌شود؛
 *     طبق بخش ۳ و ۲۵ سند، Timestamp داخل پیام MT5 هرگز مبنای محاسبات زمانی نیست.
 *   - فقط کلمه Buy/Sell بین دو حالت فرق می‌کند؛ بقیه ساختار عیناً یکسان است (تأیید کاربر).
 */
public class MetaTraderSignalParser implements SignalParser {

    // گروه‌ها: 1=Symbol, 2=Timeframe(اول), 3=Direction, 4=Timeframe(تکراری در بخش دوم - صرفاً برای تطبیق فرمت), 5=Price
    private static final Pattern PATTERN = Pattern.compile(
            "^\\(([A-Za-z0-9]+),\\s*([A-Za-z0-9]+)\\)\\s+(Buy|Sell)\\s+Signal-\\(([A-Za-z0-9]+)-(-?\\d+(?:\\.\\d+)?)\\)-\\([^\\]]+\\]\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public RawParsedSignal tryParse(String notificationText) {
        if (notificationText == null) return null;
        Matcher matcher = PATTERN.matcher(notificationText.trim());
        if (!matcher.matches()) {
            return null;
        }
        String symbol = matcher.group(1);
        String timeframe = matcher.group(2);
        String direction = matcher.group(3);
        String priceText = matcher.group(5);
        return new RawParsedSignal(symbol, timeframe, direction, priceText);
    }
}
