package com.mealorder.observer;

import com.mealorder.enums.OrderStatus;
import com.mealorder.model.Order;

/**
 * Sends SMS alerts to the rider when their order status changes.
 *
 * <p>Only fires for status transitions the rider actually cares about.
 * In production: replace {@code simulateSms()} with an Twilio/SNS API call.
 *
 * <p><b>SRP:</b> This class has exactly one job — format and send SMS.
 * It does not decide whether to send (that's the Order's responsibility via observer firing).
 */
public class SmsNotifier implements OrderObserver {

    @Override
    public void onOrderStatusChanged(Order order, OrderStatus newStatus) {
        String message = switch (newStatus) {
            case CONFIRMED   -> String.format("Order #%s confirmed! Estimated ready: %s",
                                    order.getOrderId(), order.getSlotTime());
            case READY       -> String.format("Your meal is ready! Come collect Order #%s 🍱",
                                    order.getOrderId());
            case CANCELLED   -> String.format("Order #%s has been cancelled. Refund in 3–5 days.",
                                    order.getOrderId());
            default          -> null; // PREPARING, COLLECTED — no SMS for these
        };

        if (message != null) {
            simulateSms(order.getRiderId(), message);
        }
    }

    /**
     * Simulates an SMS send. Replace with real provider (Twilio, AWS SNS) in production.
     */
    private void simulateSms(String riderId, String message) {
        System.out.printf("[SMS → Rider %s] %s%n", riderId, message);
    }
}
