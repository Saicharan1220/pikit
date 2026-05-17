package com.mealorder.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for all domain-specific exceptions.
 *
 * DECISION REASONING:
 * - All domain exceptions extend this base class (enforces consistent error handling)
 * - Maps exceptions to HTTP status codes (e.g., 404 for NOT_FOUND)
 * - Enables global exception handler to catch all business logic errors
 * - Prevents generic Exception usage (which hides intent)
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows exception hierarchy design
 * - Demonstrates proper separation of concerns (domain vs HTTP)
 * - Makes code more maintainable and testable
 */
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BaseException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
