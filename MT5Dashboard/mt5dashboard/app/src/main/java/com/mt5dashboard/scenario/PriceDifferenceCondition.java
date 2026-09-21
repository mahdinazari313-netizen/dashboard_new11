package com.mt5dashboard.scenario;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mt5dashboard.core.Signal;

/**
 * شرط Price Difference (بخش ۱۱ سند).
 *
 * === تاریخچهٔ تصمیم (مهم، برای هر کسی که بعداً این فایل را می‌خواند) ===
 * نسخهٔ اولیهٔ این شرط "سخت‌گیرانه" بود: تمام قیمت‌های داخل ترکیب باید
 * زوج‌به‌زوج متفاوت می‌بودند - همین که فقط دو سیگنال در کل ترکیب (حتی
 * تایم‌فریم‌هایی که ربطی به سیگنال اصلی نداشتند) قیمت یکسان می‌داشتند، کل
 * ترکیب رد می‌شد.
 *
 * این تفسیر بعداً توسط کاربر آگاهانه کنار گذاشته شد، با این استدلال دقیق و
 * مبتنی بر نوع واقعی سیگنال‌دهی‌اش: وقتی هم‌زمان تعداد زیادی تایم‌فریم مشغول
 * جست‌وجوی سیگنال‌اند، طبیعی و رایج است که چند تایم‌فریم نزدیک به هم تصادفاً
 * قیمت یکسان بدهند، در حالی‌که سیگنال واقعی و معنی‌دار (مثلاً روی یک تایم‌فریم پایین‌تر) با قیمتی کاملاً متفاوت هم‌زمان وجود دارد. تفسیر
 * سخت‌گیرانه در چنین حالتی کل ترکیب را رد می‌کرد - دقیقاً برخلاف خواستهٔ
 * واقعی کاربر.
 *
 * تفسیر جدید (همین نسخه): کافی‌ست در کل ترکیب حداقل ۲ مقدار قیمت *متمایز*
 * وجود داشته باشد - نه اینکه همهٔ اعضا زوج‌به‌زوج متفاوت باشند. یعنی اگر چند
 * سیگنال تصادفاً هم‌قیمت باشند ولی حداقل یکی با بقیه فرق داشته باشد، شرط
 * پاس می‌شود. اگر تمام سیگنال‌های ترکیب دقیقاً یک قیمت مشترک داشته باشند
 * (هیچ عدد متفاوتی در کار نباشد)، شرط همچنان رد می‌شود - این حالت با تفسیر
 * قدیم هم رد می‌شد، پس هیچ محافظتی از دست نرفته.
 *
 * توجه مهم دربارهٔ دامنهٔ اثر: طبق تصمیم صریح کاربر، این تغییر *فقط* وقتی
 * Price Difference فعال باشد اثر دارد (یعنی isApplicable لحظه‌ای که این
 * شرط اصلاً اجرا می‌شود). در تمام سناریوهایی که Price Difference خاموش
 * است، این کلاس اصلاً فراخوانی نمی‌شود (طبق ConditionEvaluator.evaluateAll:
 * "اگر isApplicable=false باشد، evaluate() صدا زده نمی‌شود") - پس هیچ اثری
 * روی هیچ منطق دیگری (Main Time Frame، Minimum Timeframe Sync، Dual in One
 * Timeframe) ندارد.
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

        Set<Double> distinctPrices = new HashSet<>();
        for (Signal s : combination) {
            distinctPrices.add(s.getPrice());
        }

        if (distinctPrices.size() >= 2) {
            return ConditionResult.pass(
                    "حداقل دو مقدار قیمت متمایز (" + distinctPrices.size() + " مقدار) در ترکیب وجود دارد");
        }
        return ConditionResult.fail("تمام سیگنال‌های ترکیب دقیقاً یک قیمت مشترک دارند - هیچ عدد متفاوتی نیست");
    }
}
