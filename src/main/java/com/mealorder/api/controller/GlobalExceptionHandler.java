package com.mealorder.api.controller;

import com.mealorder.api.dto.ErrorResponse;
import com.mealorder.api.util.TraceIdProvider;
import com.mealorder.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global Exception Handler for REST APIs.
 *
 * DECISION REASONING:
 * - @RestControllerAdvice: Centralized exception handling across all controllers
 * - Converts exceptions to consistent ApiResponse/ErrorResponse format
 * - Logs exceptions with trace IDs for debugging
 * - Hides stack traces in production (security)
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows understanding of Spring's exception handling mechanism
 * - Demonstrates defense in depth (consistent error responses)
 * - Proves awareness of security (stack traces leaking info)
 * - Enables better observability (all errors logged with trace ID)
 *
 * BENEFITS:
 * - Client always gets consistent error format
 * - Developers get detailed logs with trace IDs
 * - Stack traces hidden in production (security)
 * - Field-level validation errors mapped clearly
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${app.show-stack-trace:false}")
    private boolean showStackTrace;

    /**
     * Handle all domain-specific exceptions (OrderNotFoundException, InvalidOrderStateException, etc.)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            WebRequest request) {

        String traceId = TraceIdProvider.getTraceId();

        // Log exception with trace ID
        log.warn("[{}] Domain exception: {} - {}",
                traceId,
                ex.getErrorCode(),
                ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .stackTrace(showStackTrace ? getStackTrace(ex) : null)
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handle validation errors (e.g., @Valid on request body fails).
     *
     * DECISION: Maps Spring's MethodArgumentNotValidException to custom ErrorResponse
     * with field-level errors. Enables frontend to show field-specific error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String traceId = TraceIdProvider.getTraceId();
        log.warn("[{}] Validation error: {}", traceId, ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();

            fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .fieldErrors(fieldErrors)
                .stackTrace(showStackTrace ? getStackTrace(ex) : null)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Catch-all handler for unexpected exceptions.
     *
     * DECISION: Returns 500 Internal Server Error with minimal info.
     * Stack trace is logged but not exposed to client (security).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        String traceId = TraceIdProvider.getTraceId();
        log.error("[{}] Unexpected exception: {}", traceId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Contact support with trace ID: " + traceId)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .stackTrace(showStackTrace ? getStackTrace(ex) : null)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Extracts stack trace as string (for logging/debugging).
     */
    private String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
