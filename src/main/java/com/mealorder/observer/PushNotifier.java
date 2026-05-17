package com.mealorder.observer;

import com.mealorder.enums.OrderStatus;
import com.mealorder.model.Order;

/**
 * Sends mobile push notifications to the rider's device.
 *
 * <p>Push fires on every state change — gives the rider a live order tracker feel.
 * Contrast with {@link SmsNotifier} which only fires on key milestones.
 */
public class PushNotifier implements OrderObserver {

    @Override
    public void onOrderStatusChanged(Order order, OrderStatus newStatus) {
        String title = "Order Update";
        String body  = switch (newStatus) {
            case CONFIRMED  -> "Restaurant accepted your order!";
            case PREPARING  -> "Kitchen is preparing your meal 👨‍🍳";
            case READY      -> "Ready for pickup! 🍱";
            case CANCELLED  -> "Order cancelled.";
            case COLLECTED  -> "Enjoy your meal! ⭐ Rate your experience";
            default         -> null;
        };

        if (body != null) {
            simulatePush(order.getRiderId(), title, body);
        }
    }

    private void simulatePush(String riderId, String title, String body) {
        System.out.printf("[PUSH → Rider %s] %s: %s%n", riderId, title, body);
    }
}
