package com.mealorder.state;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.model.Order;

/** Terminal cancelled state. All transitions rejected. */
public class CancelledState implements OrderStateHandler {

    @Override public void confirm(Order o)       { reject(); }
    @Override public void startPreparing(Order o){ reject(); }
    @Override public void markReady(Order o)     { reject(); }
    @Override public void collect(Order o)       { reject(); }
    @Override public void cancel(Order o)        { reject(); }

    private void reject() {
        throw new InvalidStateTransitionException(OrderStatus.CANCELLED, null);
    }
}
