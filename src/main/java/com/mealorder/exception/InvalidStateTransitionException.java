package com.mealorder.exception;

import com.mealorder.enums.OrderStatus;

/**
 * Thrown when code attempts an illegal Order state transition.
 *
 * <p>Example: calling {@code cancel()} on a COLLECTED order.
 * Catching this at service boundaries lets you return a clean HTTP 409 Conflict.
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final OrderStatus from;
    private final OrderStatus to;

    public InvalidStateTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format("Cannot transition order from %s to %s", from, to));
        this.from = from;
        this.to = to;
    }

    public OrderStatus getFrom() { return from; }
    public OrderStatus getTo()   { return to;   }
}
