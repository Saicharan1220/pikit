package com.mealorder.strategy;

import com.mealorder.model.OrderItem;
import java.util.List;

/**
 * Strategy interface for computing the total price of an order.
 *
 * <p><b>Pattern:</b> Strategy (GoF Behavioral)
 *
 * <p>By injecting a {@code PricingStrategy} into an Order at creation time,
 * the Order class has zero pricing logic. Adding a "Happy Hour" discount
 * or a "Corporate Account" rate means adding a new class — not modifying Order.
 *
 * <p><b>Real-world analogy:</b> Uber's surge pricing vs base pricing is this
 * exact pattern at the service level — same ride computation, different multiplier.
 */
public interface PricingStrategy {

    /**
     * Calculates the final price for the given line items.
     *
     * @param items the list of ordered menu items with quantities
     * @return final price in INR (rupees)
     */
    double calculateTotal(List<OrderItem> items);
}
