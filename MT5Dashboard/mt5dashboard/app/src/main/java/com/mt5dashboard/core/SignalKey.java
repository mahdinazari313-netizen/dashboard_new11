package com.mt5dashboard.core;

import java.util.Objects;

/**
 * کلید هویتی یک وضعیت سیگنال، طبق تصمیم نهایی و قطعی معماری:
 *
 *   Key = Symbol + Timeframe + Direction
 *
 * قوانین (تأیید شده در گفتگو):
 *   - EURUSD+H1+BUY و EURUSD+H1+SELL دو وضعیت کاملاً مستقل‌اند و هم‌زمان می‌توانند فعال باشند.
 *   - سیگنال جدید با همین کلید دقیق، فقط سیگنال قبلی با همان کلید را جایگزین می‌کند؛
 *     جهت مقابل (BUY/SELL دیگر) هیچ تغییری نمی‌کند.
 *
 * هشدار (بخش ۲۵ سند، اصل جلوگیری از خطای طراحی):
 *   Direction هرگز نباید از این کلید حذف شود مگر با تصمیم صریح و مکتوب طراح.
 */
public final class SignalKey {

    private final String symbol;
    private final Timeframe timeframe;
    private final Direction direction;

    public SignalKey(String symbol, Timeframe timeframe, Direction direction) {
        if (symbol == null || timeframe == null || direction == null) {
            throw new IllegalArgumentException("symbol, timeframe و direction نمی‌توانند null باشند");
        }
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.direction = direction;
    }

    public String getSymbol() {
        return symbol;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignalKey)) return false;
        SignalKey that = (SignalKey) o;
        return symbol.equals(that.symbol)
                && timeframe == that.timeframe
                && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, timeframe, direction);
    }

    @Override
    public String toString() {
        return symbol + "+" + timeframe + "+" + direction;
    }
}
