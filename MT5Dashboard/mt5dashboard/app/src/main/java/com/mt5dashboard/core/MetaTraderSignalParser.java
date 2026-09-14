package com.mt5dashboard.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses the confirmed MT5 notification format. */
public class MetaTraderSignalParser implements SignalParser {

    /*
     * Broker symbols may contain underscores and other common symbol characters
     * (for example AUDNZD_i). Keep the parser strict about the message structure,
     * but do not incorrectly reject valid broker symbol names.
     * Groups: 1=symbol, 2=timeframe, 3=direction, 4=embedded timeframe, 5=price.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "^\\(([^(),\\s]+),\\s*([A-Za-z][A-Za-z0-9]*)\\)\\s+(Buy|Sell)\\s+Signal-\\(([A-Za-z][A-Za-z0-9]*)-(-?\\d+(?:\\.\\d+)?)\\)-\\([^\\]]+\\]\\s*$",
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
