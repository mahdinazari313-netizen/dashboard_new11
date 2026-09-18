package com.mt5dashboard.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses the confirmed MT5 notification format. */
public class MetaTraderSignalParser implements SignalParser {

    /*
     * Broker symbols may contain underscores and other common symbol characters
     * (for example AUDNZD_i, EURAUD.x). Keep the parser strict about the message
     * structure, but do not incorrectly reject valid broker symbol names.
     * Groups: 1=symbol, 2=timeframe, 3=direction, 4=embedded timeframe, 5=price.
     *
     * FIX: the trailing date/time block used to open with "(" and close with
     * "]" (asymmetric), e.g. "-(2026.08.25 20:48:00]". The real notifications
     * now open it with "[" instead, e.g. "-[2026.09.17 02:04:00]" (confirmed
     * from actual device screenshots: "(EURAUD.x,M1) Buy Signal-(M1-1.61509)-
     * [2026.09.17 02:04:00]"). Every message failed to match before this fix,
     * so no signal was ever recorded. Only that one bracket character changed;
     * everything else about the format (including the timeframe-consistency
     * check below) stayed the same.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "^\\(([^(),\\s]+),\\s*([A-Za-z][A-Za-z0-9]*)\\)\\s+(Buy|Sell)\\s+Signal-\\(([A-Za-z][A-Za-z0-9]*)-(-?\\d+(?:\\.\\d+)?)\\)-\\[[^\\]]+\\]\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public RawParsedSignal tryParse(String notificationText) {
        if (notificationText == null) return null;
        Matcher matcher = PATTERN.matcher(notificationText.trim());
        if (!matcher.matches()) return null;

        String symbol = matcher.group(1);
        String timeframe = matcher.group(2);
        String direction = matcher.group(3);
        String embeddedTimeframe = matcher.group(4);

        // Prevent accepting a malformed message where the two timeframes differ.
        if (!timeframe.equalsIgnoreCase(embeddedTimeframe)) return null;

        String priceText = matcher.group(5);
        return new RawParsedSignal(symbol, timeframe, direction, priceText);
    }
}
