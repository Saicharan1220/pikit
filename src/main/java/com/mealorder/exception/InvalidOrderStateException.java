package com.mealorder.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an order transition to an invalid state.
 *
 * DECISION REASONING:
 * - Specific exception for business logic violations (400 Bad Request)
 * - Prevents invalid state transitions (e.g., cannot confirm completed order)
 * - State machine validation at service layer (not database)
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows understanding of state machine patterns
 * - Demonstrates proper validation at business logic layer
 * - Prevents database corruption from invalid state changes
 */
public class InvalidOrderStateException extends BaseException {

    public InvalidOrderStateException(String currentState, String attemptedState) {
        super(
            "INVALID_ORDER_STATE",
            String.format("Cannot transition from %s to %s", currentState, attemptedState),
            HttpStatus.BAD_REQUEST
        );
    }

    public InvalidOrderStateException(String message) {
        super(
            "INVALID_ORDER_STATE",
            message,
            HttpStatus.BAD_REQUEST
        );
    }
}
