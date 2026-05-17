package com.mealorder.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when no delivery slots are available.
 *
 * DECISION REASONING:
 * - Business constraint: Cannot accept more orders than available slots
 * - Maps to HTTP 409 Conflict (resource conflict)
 * - Atomic slot checking prevents double-booking (CAS operation with AtomicInteger)
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows understanding of resource constraints
 * - Demonstrates atomic operations for thread safety
 * - Proper HTTP status code selection (409 Conflict vs 400 Bad Request)
 */
public class InsufficientSlotsException extends BaseException {

    public InsufficientSlotsException(String message) {
        super(
            "INSUFFICIENT_SLOTS",
            message,
            HttpStatus.CONFLICT
        );
    }
}
