package com.mt5dashboard.scenario;

import java.util.Arrays;
import java.util.List;

import com.mt5dashboard.core.Signal;

/**
 * تمام Conditionهای موجود را روی یک ترکیب اجرا می‌کند.
 * فقط Conditionهایی که isApplicable=true هستند بررسی می‌شوند (بخش ۱۱: شروط
 * قابل فعال/غیرفعال شدن‌اند). افزودن Condition جدید فقط نیاز به اضافه کردن
 * آن به لیست conditions دارد؛ بقیه سیستم دست‌نخورده می‌ماند.
 */
public class ConditionEvaluator {

    private final List<Condition> conditions = Arrays.asList(
            new MainTimeFrameCondition(),
            new PriceDifferenceCondition()
            // شروط آینده (مثلاً TimeWindowCondition) اینجا اضافه می‌شوند - بدون تغییر بقیه ScenarioEngine
    );

    /**
     * @return نتیجه اولین شرط ردشده، یا یک ConditionResult.pass اگر همه شروط فعال پاس شدند.
     */
    public ConditionResult evaluateAll(List<Signal> combination, ScenarioConfig config) {
        for (Condition condition : conditions) {
            if (!condition.isApplicable(config)) {
                continue;
            }
            ConditionResult result = condition.evaluate(combination, config);
            if (!result.passed) {
                return result;
            }
        }
        return ConditionResult.pass("تمام شروط فعال پاس شدند");
    }
}
