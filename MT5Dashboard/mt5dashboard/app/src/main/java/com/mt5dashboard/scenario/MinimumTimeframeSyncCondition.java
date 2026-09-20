package com.mt5dashboard.scenario;

import java.util.List;

import com.mt5dashboard.core.Signal;

/**
 * شرط «حداقل تعداد تایم‌فریم برای هم‌زمانی».
 *
 * اگر فعال باشد، ترکیب فعلی باید حداقل به تعداد مشخص‌شده سیگنال از
 * تایم‌فریم‌های متفاوت داشته باشد تا Trigger مجاز شود. این شرط مستقل از
 * Main Time Frame و Price Difference است.
 *
 * در SignalStateManager کلید هر سیگنال Symbol+Timeframe+Direction است؛
 * بنابراین برای یک Symbol+Direction، در هر Timeframe فقط یک سیگنال جاری
 * وجود دارد و combination.size() برابر با تعداد Timeframeهای حاضر است.
 */
public class MinimumTimeframeSyncCondition implements Condition {

    @Override
    public boolean isApplicable(ScenarioConfig config) {
        return config.isMinimumTimeframeSyncEnabled();
    }

    @Override
    public ConditionResult evaluate(List<Signal> combination, ScenarioConfig config) {
        int required = config.getMinimumTimeframeCount();

        if (combination.size() >= required) {
            return ConditionResult.pass(
                    "تعداد تایم‌فریم‌های هم‌زمان (" + combination.size()
                            + ") به حداقل لازم (" + required + ") رسیده است");
        }

        return ConditionResult.fail(
                "تعداد تایم‌فریم‌های هم‌زمان (" + combination.size()
                        + ") کمتر از حداقل لازم (" + required + ") است");
    }
}
