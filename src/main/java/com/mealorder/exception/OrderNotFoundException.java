package com.mealorder.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an order is not found in the system.
 *
 * DECISION REASONING:
 * - Specific exception for missing orders (404 Not Found)
 * - Maps to HTTP 404 automatically via global exception handler
 * - Clear error code for debugging and frontend handling
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Demonstrates domain-driven exception design
 * - Shows understanding of HTTP semantics (404 for missing resources)
 */
public class OrderNotFoundException extends BaseException {

    public OrderNotFoundException(Long orderId) {
        super(
            "ORDER_NOT_FOUND",
            String.format("Order with ID %d not found", orderId),
            HttpStatus.NOT_FOUND
        );
    }

    public OrderNotFoundException(String message) {
        super(
            "ORDER_NOT_FOUND",
            message,
            HttpStatus.NOT_FOUND
        );
    }
}
