package com.mt5dashboard.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mt5dashboard.core.Direction;
import com.mt5dashboard.core.Signal;

/**
 * یک Trigger یعنی یک ترکیب موفق (شروط سناریو را پاس کرده) در یک لحظه مشخص.
 *
 * طبق توافق قبلی: Trigger شامل symbol و direction است (چون Scenario خودش
 * به این دو مقید نیست - Trigger مشخص می‌کند این خروجی برای کدام Symbol/Direction است).
 *
 * هویت Trigger با هر رویداد سیگنال جدید (که شروط را دوباره پاس کند) تغییر می‌کند؛
 * یعنی id هر Trigger یکتا و مخصوص همان لحظه صدور است (بخش ۱۳ و ۱۴ سند).
 */
public class Trigger {

    private final String id;
    private final String scenarioId;
    private final String symbol;
    private final Direction direction;
    private final List<Signal> combination; // سیگنال‌های تشکیل‌دهنده این ترکیب، در لحظه صدور
    private final long createdAt;

    private boolean silenced = false;
    private long lastAlarmAt;

    public Trigger(String scenarioId, String symbol, Direction direction,
                    List<Signal> combination, long createdAt) {
        this.id = UUID.randomUUID().toString();
        this.scenarioId = scenarioId;
        this.symbol = symbol;
        this.direction = direction;
        this.combination = new ArrayList<>(combination);
        this.createdAt = createdAt;
        this.lastAlarmAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Direction getDirection() {
        return direction;
    }

    public List<Signal> getCombination() {
        return combination;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isSilenced() {
        return silenced;
    }

    /** بخش ۱۴ سند: Silent فقط مربوط به همین Trigger فعلی است. */
    public void silence() {
        this.silenced = true;
    }

    public long getLastAlarmAt() {
        return lastAlarmAt;
    }

    public void markAlarmFired(long now) {
        this.lastAlarmAt = now;
    }

    /**
     * مجموعه شناسه‌های سیگنال‌های تشکیل‌دهنده این ترکیب.
     * برای تشخیص اینکه آیا ترکیب "همان قبلی" است یا یک رویداد جدید
     * (استفاده در ScenarioEngine برای Safety Check دوره‌ای، نه برای رویداد سیگنال جدید).
     */
    public java.util.Set<String> getSignalIdSet() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (Signal s : combination) {
            ids.add(s.getId());
        }
        return ids;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trigger{scenario=").append(scenarioId)
                .append(", symbol=").append(symbol)
                .append(", direction=").append(direction)
                .append(", combination=[");
        for (Signal s : combination) {
            sb.append(s.getTimeframe()).append("@").append(s.getPrice()).append(" ");
        }
        sb.append("], silenced=").append(silenced).append("}");
        return sb.toString();
    }
}
