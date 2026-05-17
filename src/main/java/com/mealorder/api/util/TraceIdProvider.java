package com.mealorder.api.util;

import org.slf4j.MDC;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

/**
 * Provides trace ID from Spring Cloud Sleuth.
 *
 * DECISION REASONING:
 * - Spring Cloud Sleuth auto-generates trace IDs for each request
 * - Used in error responses and logs for distributed tracing
 * - Enables linking logs across multiple services
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Demonstrates understanding of distributed tracing
 * - Shows awareness of observability in microservices
 * - Enables faster debugging in production
 *
 * HOW IT WORKS:
 * 1. Request arrives: Sleuth auto-generates traceId (a1b2c3d4)
 * 2. TraceIdProvider extracts it from MDC
 * 3. Error response includes traceId
 * 4. Client can report error with traceId
 * 5. Operator searches logs with that traceId across services
 */
@Component
public class TraceIdProvider {

    private static Tracer tracer;

    public TraceIdProvider(Tracer tracer) {
        TraceIdProvider.tracer = tracer;
    }

    /**
     * Get current trace ID from Sleuth context.
     */
    public static String getTraceId() {
        if (tracer != null && tracer.currentTraceContext().context() != null) {
            return tracer.currentTraceContext().context().traceId();
        }
        // Fallback to MDC if Sleuth context unavailable
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "UNKNOWN";
    }
}
