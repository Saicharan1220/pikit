package com.mealorder.strategy;

import com.mealorder.model.OrderItem;
import java.util.List;

/**
 * VIP / loyalty member pricing — applies a flat 15% discount on subtotal.
 *
 * <p><b>Extension idea:</b> Make the discount percentage configurable via
 * constructor injection so product managers can change it without a code deploy.
 */
public class VipPricingStrategy implements PricingStrategy {

    private static final double VIP_DISCOUNT = 0.15;

    private final PricingStrategy base;

    /**
     * Wraps a base strategy and applies the VIP discount on top.
     * Decorator-like composition: VIP = Regular × (1 - discount).
     */
    public VipPricingStrategy(PricingStrategy base) {
        this.base = base;
    }

    @Override
    public double calculateTotal(List<OrderItem> items) {
        double subtotal = base.calculateTotal(items);
        return subtotal * (1 - VIP_DISCOUNT);
    }
}
