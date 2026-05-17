package com.mealorder.observer;

import com.mealorder.enums.OrderStatus;
import com.mealorder.model.Order;

/**
 * Sends email receipts on order confirmation and collection.
 *
 * <p>Email is used only for durable records (confirmation + receipt),
 * not for real-time updates — that's what Push is for.
 * This selective firing keeps email non-spammy and purposeful.
 */
public class EmailNotifier implements OrderObserver {

    @Override
    public void onOrderStatusChanged(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CONFIRMED) {
            simulateEmail(order.getRiderId(),
                "Order Confirmed — #" + order.getOrderId(),
                buildConfirmationBody(order));
        } else if (newStatus == OrderStatus.COLLECTED) {
            simulateEmail(order.getRiderId(),
                "Your Receipt — Order #" + order.getOrderId(),
                buildReceiptBody(order));
        }
    }

    private String buildConfirmationBody(Order order) {
        return String.format(
            "Hi! Your order #%s has been confirmed.\nPickup slot: %s\nTotal: ₹%.2f",
            order.getOrderId(), order.getSlotTime(), order.getTotalAmount());
    }

    private String buildReceiptBody(Order order) {
        return String.format(
            "Thanks for using MealOrder!\nOrder #%s — ₹%.2f collected successfully.",
            order.getOrderId(), order.getTotalAmount());
    }

    private void simulateEmail(String riderId, String subject, String body) {
        System.out.printf("[EMAIL → Rider %s] Subject: %s%n  %s%n", riderId, subject, body);
    }
}
