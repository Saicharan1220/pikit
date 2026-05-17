package com.mealorder.strategy;

import com.mealorder.model.OrderItem;
import java.time.LocalTime;
import java.util.List;

/**
 * Peak-hour surge pricing — adds a 20% surcharge during 12:00–14:00 and 19:00–21:00.
 *
 * <p><b>Interview talking point:</b> The strategy is injected at order-creation time
 * by the service layer, which knows the current time. The Order itself is time-agnostic.
 * This makes the Order trivially testable without mocking clocks.
 */
public class PeakHourPricingStrategy implements PricingStrategy {

    private static final double SURGE_MULTIPLIER = 1.20;

    private final PricingStrategy base;
    private final LocalTime orderTime;

    public PeakHourPricingStrategy(PricingStrategy base, LocalTime orderTime) {
        this.base = base;
        this.orderTime = orderTime;
    }

    @Override
    public double calculateTotal(List<OrderItem> items) {
        double subtotal = base.calculateTotal(items);
        return isPeakHour() ? subtotal * SURGE_MULTIPLIER : subtotal;
    }

    private boolean isPeakHour() {
        return isBetween(orderTime, LocalTime.of(12, 0), LocalTime.of(14, 0))
            || isBetween(orderTime, LocalTime.of(19, 0), LocalTime.of(21, 0));
    }

    private boolean isBetween(LocalTime t, LocalTime start, LocalTime end) {
        return !t.isBefore(start) && t.isBefore(end);
    }
}
