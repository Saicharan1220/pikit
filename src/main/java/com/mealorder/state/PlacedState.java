package com.mealorder.state;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.model.Order;

/**
 * Behaviour of an Order immediately after it is placed by a rider.
 *
 * <p>Valid transitions from PLACED:
 * <ul>
 *   <li>{@code confirm()} → moves to CONFIRMED
 *   <li>{@code cancel()} → moves to CANCELLED
 * </ul>
 */
public class PlacedState implements OrderStateHandler {

    @Override
    public void confirm(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStateHandler(new ConfirmedState());
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setStateHandler(new CancelledState());
    }

    @Override
    public void startPreparing(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.PLACED, OrderStatus.PREPARING);
    }

    @Override
    public void markReady(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.PLACED, OrderStatus.READY);
    }

    @Override
    public void collect(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.PLACED, OrderStatus.COLLECTED);
    }
}
