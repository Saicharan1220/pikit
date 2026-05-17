package com.mealorder.exception;

/**
 * Thrown when an Order lookup returns no result.
 * Maps to HTTP 404 at the API layer.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
