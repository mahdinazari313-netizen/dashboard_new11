package com.mt5dashboard.scenario;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mt5dashboard.core.Signal;

/**
 * شرط Price Difference (بخش ۱۱ سند):
 * «قیمت‌های موجود در ترکیب باید متفاوت باشند... اگر حتی قیمت‌ها دقیقاً برابر
 * باشند، شرط رد می‌شود.»
 *
 * تفسیر دقیق: تمام قیمت‌های داخل ترکیب باید زوج‌به‌زوج متفاوت باشند (نه فقط
 * «همه یکسان نباشند»). اگر حتی دو سیگنال در ترکیب قیمت دقیقاً یکسان داشته
 * باشند، شرط رد می‌شود؛ چون متن صریحاً می‌گوید قیمت‌ها [همگی] باید متفاوت باشند.
 *
 * اگر ترکیب فقط یک سیگنال داشته باشد، مقایسه‌ای معنی ندارد، پس شرط پاس می‌شود.
 *
 * بدون هیچ Threshold برای pip/point/درصد (بخش ۷ سند) - مقایسه دقیق == روی double.
 */
public class PriceDifferenceCondition implements Condition {

    @Override
    public boolean isApplicable(ScenarioConfig config) {
        return config.isPriceDifferenceEnabled();
    }

    @Override
    public ConditionResult evaluate(List<Signal> combination, ScenarioConfig config) {
        if (combination.size() < 2) {
            return ConditionResult.pass("فقط یک سیگنال در ترکیب است، مقایسه قیمت لازم نیست");
        }

        Set<Double> seenPrices = new HashSet<>();
        for (Signal s : combination) {
            if (!seenPrices.add(s.getPrice())) {
                return ConditionResult.fail("حداقل دو سیگنال در ترکیب قیمت یکسان (" + s.getPrice() + ") دارند");
            }
        }
        return ConditionResult.pass("تمام قیمت‌های ترکیب زوج‌به‌زوج متفاوت‌اند");
    }
}
