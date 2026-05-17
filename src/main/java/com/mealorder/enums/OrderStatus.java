package com.mealorder.enums;

/**
 * Every valid lifecycle state an Order can occupy.
 *
 * <p>Valid transitions (enforced by the State Pattern in {@link com.mealorder.state}):
 * <pre>
 *   PLACED → CONFIRMED → PREPARING → READY → COLLECTED
 *     ↓           ↓
 *  CANCELLED   CANCELLED
 * </pre>
 *
 * <p><b>LLD Note:</b> Enum over String constants — the compiler catches typos,
 * and exhaustive switch expressions eliminate missing-case bugs.
 */
public enum OrderStatus {
    PLACED, CONFIRMED, PREPARING, READY, COLLECTED, CANCELLED
}
