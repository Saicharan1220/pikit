package com.mealorder.state;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.model.Order;

/**
 * Behaviour of a confirmed Order — restaurant has accepted, kitchen not yet started.
 *
 * <p>Valid transitions: startPreparing, cancel.
 */
public class ConfirmedState implements OrderStateHandler {

    @Override
    public void startPreparing(Order order) {
        order.setStatus(OrderStatus.PREPARING);
        order.setStateHandler(new PreparingState());
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setStateHandler(new CancelledState());
    }

    @Override
    public void confirm(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.CONFIRMED, OrderStatus.CONFIRMED);
    }

    @Override
    public void markReady(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.CONFIRMED, OrderStatus.READY);
    }

    @Override
    public void collect(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.CONFIRMED, OrderStatus.COLLECTED);
    }
}
