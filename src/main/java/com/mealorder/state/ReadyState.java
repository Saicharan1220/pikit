package com.mealorder.state;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.model.Order;

/**
 * Meal is packaged and waiting at the counter.
 * Only valid action: collect.
 */
public class ReadyState implements OrderStateHandler {

    @Override
    public void collect(Order order) {
        order.setStatus(OrderStatus.COLLECTED);
        order.setStateHandler(new CollectedState());
    }

    @Override
    public void confirm(Order order)       { throwIllegal(OrderStatus.READY, OrderStatus.CONFIRMED);  }
    @Override
    public void startPreparing(Order order){ throwIllegal(OrderStatus.READY, OrderStatus.PREPARING);  }
    @Override
    public void markReady(Order order)     { throwIllegal(OrderStatus.READY, OrderStatus.READY);      }
    @Override
    public void cancel(Order order)        { throwIllegal(OrderStatus.READY, OrderStatus.CANCELLED);  }

    private void throwIllegal(OrderStatus from, OrderStatus to) {
        throw new InvalidStateTransitionException(from, to);
    }
}
