package com.mealorder.state;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.model.Order;

/**
 * Behaviour while the kitchen is actively preparing the meal.
 *
 * <p>Once preparing starts, the order cannot be cancelled — food costs are committed.
 * Only valid next step: markReady.
 */
public class PreparingState implements OrderStateHandler {

    @Override
    public void markReady(Order order) {
        order.setStatus(OrderStatus.READY);
        order.setStateHandler(new ReadyState());
    }

    @Override
    public void cancel(Order order) {
        // Kitchen has started — cancellation no longer possible
        throw new InvalidStateTransitionException(OrderStatus.PREPARING, OrderStatus.CANCELLED);
    }

    @Override
    public void confirm(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.PREPARING, OrderStatus.CONFIRMED);
    }

    @Override
    public void startPreparing(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.PREPARING, OrderStatus.PREPARING);
    }

    @Override
    public void collect(Order order) {
        throw new InvalidStateTransitionException(OrderStatus.PREPARING, OrderStatus.COLLECTED);
    }
}
