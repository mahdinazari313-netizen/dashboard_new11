package com.mt5dashboard.core;

/**
 * خروجی خام لایه Parser، پیش از هرگونه اعتبارسنجی.
 * عمداً از String استفاده شده (نه Enum) چون در این مرحله هنوز معلوم نیست
 * مقادیر معتبرند یا نه؛ تصمیم رد/قبول بر عهده SignalValidator است.
 */
public final class RawParsedSignal {
    public final String symbol;
    public final String timeframe;
    public final String direction;
    public final String priceText;

    public RawParsedSignal(String symbol, String timeframe, String direction, String priceText) {
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.direction = direction;
        this.priceText = priceText;
    }

    @Override
    public String toString() {
        return "RawParsedSignal{symbol='" + symbol + "', timeframe='" + timeframe +
                "', direction='" + direction + "', priceText='" + priceText + "'}";
    }
}
