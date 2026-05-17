package com.mealorder.factory;

import com.mealorder.enums.OrderType;
import com.mealorder.model.Order;
import com.mealorder.model.OrderItem;
import com.mealorder.model.Rider;
import com.mealorder.observer.EmailNotifier;
import com.mealorder.observer.PushNotifier;
import com.mealorder.observer.SmsNotifier;
import com.mealorder.strategy.PeakHourPricingStrategy;
import com.mealorder.strategy.PricingStrategy;
import com.mealorder.strategy.RegularPricingStrategy;
import com.mealorder.strategy.VipPricingStrategy;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Centralises the creation of {@link Order} objects.
 *
 * <p><b>Pattern:</b> Factory (GoF Creational)
 *
 * <p><b>Why Factory?</b> Creating an Order involves:
 * <ol>
 *   <li>Choosing the correct {@link PricingStrategy} based on rider VIP status + time of day
 *   <li>Wiring the correct {@link com.mealorder.observer.OrderObserver}s
 *   <li>Generating an idempotency key
 *   <li>Validating all inputs before construction
 * </ol>
 * If callers did all this directly, every call site would repeat the same
 * logic — and be responsible for keeping it in sync. Factory owns it once.
 *
 * <p><b>SOLID:</b> Callers depend on {@code OrderFactory}, not on the
 * Order's internal construction details. If construction changes (e.g., adding
 * group orders), only this class changes.
 */
public class OrderFactory {

    /**
     * Creates a fully wired Order with the appropriate pricing strategy
     * and notification observers pre-registered.
     *
     * @param rider        the rider placing the order
     * @param restaurantId target restaurant's ID
     * @param items        list of items with quantities
     * @param orderType    PRE_ORDER or INSTANT
     * @param slotTime     human-readable slot (e.g. "12:30 PM") — null for INSTANT
     * @return a ready-to-use Order in PLACED state
     */
    public Order createOrder(Rider rider, String restaurantId,
                             List<OrderItem> items, OrderType orderType, String slotTime) {

        PricingStrategy strategy = buildPricingStrategy(rider);

        Order order = new Order.Builder()
            .riderId(rider.getRiderId())
            .restaurantId(restaurantId)
            .items(items)
            .orderType(orderType)
            .pricingStrategy(strategy)
            .slotTime(slotTime != null ? slotTime : "ASAP")
            .idempotencyKey(UUID.randomUUID().toString())
            .build();

        // Register default observers — all orders get Push + SMS
        order.addObserver(new PushNotifier());
        order.addObserver(new SmsNotifier());
        // Email only for confirmed orders (to avoid spam on every status)
        order.addObserver(new EmailNotifier());

        System.out.printf("[OrderFactory] Created %s order %s for rider %s | strategy: %s | total: ₹%.2f%n",
            orderType, order.getOrderId(), rider.getName(),
            strategy.getClass().getSimpleName(), order.getTotalAmount());

        return order;
    }

    /**
     * Selects the pricing strategy based on rider status and current time.
     *
     * <p>Strategy composition: PeakHour wraps VIP wraps Regular.
     * The decorator chain applies modifiers in order from inside out.
     *
     * <p><b>Interview question:</b> "How do you handle a VIP user during peak hours?"
     * → VIP discount applies to the base price, then peak surcharge on top.
     * The order of composition is a business decision encoded here explicitly.
     */
    private PricingStrategy buildPricingStrategy(Rider rider) {
        PricingStrategy strategy = new RegularPricingStrategy();

        if (rider.isVip()) {
            strategy = new VipPricingStrategy(strategy);
        }

        // Check if current time is a peak hour
        LocalTime now = LocalTime.now();
        boolean isPeak = isPeakHour(now);
        if (isPeak) {
            strategy = new PeakHourPricingStrategy(strategy, now);
        }

        return strategy;
    }

    private boolean isPeakHour(LocalTime t) {
        return (t.isAfter(LocalTime.of(11, 59)) && t.isBefore(LocalTime.of(14, 0)))
            || (t.isAfter(LocalTime.of(18, 59)) && t.isBefore(LocalTime.of(21, 0)));
    }
}
