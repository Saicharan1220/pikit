package com.mealorder.enums;

/**
 * Distinguishes pre-scheduled meals from on-demand orders.
 * Used by {@link com.mealorder.factory.OrderFactory} to instantiate the correct subtype.
 */
public enum OrderType {
    /** Rider selects a future pickup slot. */
    PRE_ORDER,
    /** Rider wants the meal as soon as possible. */
    INSTANT
}
