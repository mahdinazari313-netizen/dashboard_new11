package com.mt5dashboard.scenario;

import java.util.List;

import com.mt5dashboard.core.Signal;

/**
 * قرارداد یک شرط سناریو (Strategy Pattern).
 * هر شرط فقط روی «ترکیب فعلی سیگنال‌های معتبر یک Symbol+Direction» قضاوت می‌کند؛
 * هیچ شرطی از وجود سناریوهای دیگر یا داده‌ای بیرون از این ترکیب خبر ندارد
 * (اصل ۲۴: استقلال کامل).
 *
 * افزودن شرط جدید در آینده (مثلاً TimeWindowCondition) فقط نیاز به یک
 * پیاده‌سازی جدید از این Interface دارد؛ ScenarioEngine نیازی به تغییر ندارد.
 */
public interface Condition {

    /**
     * آیا این شرط اصلاً برای این Scenario فعال است؟
     * اگر false برگرداند، evaluate() صدا زده نمی‌شود (شرط نادیده گرفته می‌شود).
     */
    boolean isApplicable(ScenarioConfig config);

    /**
     * ارزیابی شرط روی ترکیب فعلی. combination هرگز خالی نیست
     * (ScenarioEngine قبل از فراخوانی Conditionها این را تضمین می‌کند).
     */
    ConditionResult evaluate(List<Signal> combination, ScenarioConfig config);
}
