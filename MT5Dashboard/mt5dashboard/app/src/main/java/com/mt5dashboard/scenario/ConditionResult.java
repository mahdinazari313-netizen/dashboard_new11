package com.mt5dashboard.scenario;

/**
 * نتیجه ارزیابی یک Condition. reason برای دیباگ/نمایش در Board کاربرد دارد،
 * نه برای منطق تصمیم‌گیری.
 */
public final class ConditionResult {
    public final boolean passed;
    public final String reason;

    private ConditionResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public static ConditionResult pass(String reason) {
        return new ConditionResult(true, reason);
    }

    public static ConditionResult fail(String reason) {
        return new ConditionResult(false, reason);
    }
}
