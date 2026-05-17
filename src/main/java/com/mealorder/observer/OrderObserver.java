package com.mealorder.observer;

import com.mealorder.enums.OrderStatus;
import com.mealorder.model.Order;

/**
 * Observer interface for order lifecycle events.
 *
 * <p><b>Pattern:</b> Observer (GoF Behavioral)
 *
 * <p>Any class interested in order state changes implements this interface
 * and registers itself on an {@link com.mealorder.model.Order}. The Order
 * broadcasts state changes without knowing anything about who is listening.
 *
 * <p><b>SOLID:</b>
 * <ul>
 *   <li>OCP — add WhatsApp notifications by implementing this interface, zero Order changes
 *   <li>DIP — Order depends on this abstraction, not on SMSService or PushService directly
 * </ul>
 *
 * <p><b>Scale-up path:</b> In production, replace synchronous observer calls
 * with Kafka events. The interface contract stays the same — only the wiring changes.
 */
public interface OrderObserver {

    /**
     * Called every time an Order's status changes.
     *
     * @param order     the order that changed (contains full context)
     * @param newStatus the status the order just transitioned into
     */
    void onOrderStatusChanged(Order order, OrderStatus newStatus);
}
