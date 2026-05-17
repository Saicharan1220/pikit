package com.mealorder.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API Response wrapper for all endpoints.
 *
 * DECISION REASONING:
 * - Wraps ALL responses in consistent structure (data, message, timestamp, status)
 * - Allows adding metadata without breaking API contracts
 * - Enables versioning, caching headers, trace IDs in future
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows understanding of API contract design
 * - Prevents tight coupling between client and server
 * - Facilitates future features (pagination, rate limiting) without breaking changes
 *
 * EXAMPLE RESPONSE:
 * {
 *   "status": "SUCCESS",
 *   "data": { "orderId": 123, "status": "CONFIRMED" },
 *   "message": "Order placed successfully",
 *   "timestamp": "2026-05-17T10:30:45.123456",
 *   "traceId": "a1b2c3d4"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    @JsonProperty("status")
    private String status;  // "SUCCESS", "ERROR", "VALIDATION_ERROR"

    @JsonProperty("data")
    private T data;  // Generic payload (Order, List<Order>, etc.)

    @JsonProperty("message")
    private String message;  // Human-readable message

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("traceId")
    private String traceId;  // For distributed tracing (Sleuth)

    // Builder shortcuts for common cases
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
