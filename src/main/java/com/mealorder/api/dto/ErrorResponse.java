package com.mealorder.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error Response DTO for failed requests.
 *
 * DECISION REASONING:
 * - Separate from ApiResponse to handle validation/error cases distinctly
 * - Includes error code for frontend internationalization
 * - Includes field-level errors for validation failures
 * - Includes stack trace in development (hidden in production)
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows understanding of proper error handling
 * - Demonstrates attention to debugging needs (stack trace in dev)
 * - Field-level errors enable proper form validation feedback
 *
 * EXAMPLE RESPONSE:
 * {
 *   "errorCode": "INVALID_ORDER_STATE",
 *   "message": "Order is already confirmed",
 *   "timestamp": "2026-05-17T10:30:45.123456",
 *   "path": "/api/v1/orders/123/confirm",
 *   "fieldErrors": [
 *     {
 *       "field": "quantity",
 *       "message": "Quantity must be between 1 and 100",
 *       "rejectedValue": 0
 *     }
 *   ],
 *   "stackTrace": "... (only in dev)"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @JsonProperty("errorCode")
    private String errorCode;  // E.g., INVALID_ORDER_STATE, RESTAURANT_NOT_FOUND

    @JsonProperty("message")
    private String message;  // Human-readable error description

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("path")
    private String path;  // Request path that caused error

    @JsonProperty("fieldErrors")
    private List<FieldError> fieldErrors;  // Validation errors per field

    @JsonProperty("stackTrace")
    private String stackTrace;  // Only in development profile

    /**
     * Nested class for field-level validation errors.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldError {
        @JsonProperty("field")
        private String field;

        @JsonProperty("message")
        private String message;

        @JsonProperty("rejectedValue")
        private Object rejectedValue;
    }
}
