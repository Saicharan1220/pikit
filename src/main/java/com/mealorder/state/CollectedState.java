package com.mealorder.state;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.model.Order;

/**
 * Terminal state — order has been collected. No further transitions possible.
 *
 * <p>All methods throw {@link InvalidStateTransitionException}.
 * This is intentional: a terminal state should loudly reject any further actions
 * rather than silently doing nothing.
 */
public class CollectedState implements OrderStateHandler {

    @Override public void confirm(Order o)       { reject(OrderStatus.COLLECTED); }
    @Override public void startPreparing(Order o){ reject(OrderStatus.COLLECTED); }
    @Override public void markReady(Order o)     { reject(OrderStatus.COLLECTED); }
    @Override public void collect(Order o)       { reject(OrderStatus.COLLECTED); }
    @Override public void cancel(Order o)        { reject(OrderStatus.COLLECTED); }

    private void reject(OrderStatus from) {
        throw new InvalidStateTransitionException(from, null);
    }
}
