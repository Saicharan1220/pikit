package com.mealorder.enums;

/**
 * Delivery channel for order status notifications.
 * Each concrete {@link com.mealorder.observer.OrderObserver} handles one channel.
 */
public enum NotificationType {
    SMS, PUSH, EMAIL
}
