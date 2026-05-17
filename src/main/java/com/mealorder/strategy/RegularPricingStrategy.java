package com.mealorder.strategy;

import com.mealorder.model.OrderItem;
import java.util.List;

/**
 * Standard pricing — sum of (unit price × quantity) for each item.
 * The baseline strategy used for most orders.
 */
public class RegularPricingStrategy implements PricingStrategy {

    @Override
    public double calculateTotal(List<OrderItem> items) {
        return items.stream()
            .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
            .sum();
    }
}
