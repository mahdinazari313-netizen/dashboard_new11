package com.mt5dashboard.scenario;

/**
 * آخرین قیمت متفاوتی که برای Main Time Frame ثبت شده، به‌همراه زمان دریافت آن.
 * این شیء Immutable است و با هر قیمت متفاوت، نمونه جدید جایگزین می‌شود.
 */
final class DualTimeframeReference {
    final double price;
    final long receivedAt;

    DualTimeframeReference(double price, long receivedAt) {
        this.price = price;
        this.receivedAt = receivedAt;
    }
}
