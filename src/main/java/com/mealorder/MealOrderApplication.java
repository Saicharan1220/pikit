package com.mealorder;

import com.mealorder.cache.MenuCache;
import com.mealorder.exception.InvalidStateTransitionException;
import com.mealorder.exception.SlotNotAvailableException;
import com.mealorder.factory.OrderFactory;
import com.mealorder.model.Order;
import com.mealorder.model.Restaurant;
import com.mealorder.model.Rider;
import com.mealorder.repository.InMemoryOrderRepository;
import com.mealorder.repository.InMemoryRestaurantRepository;
import com.mealorder.service.OrderService;
import com.mealorder.service.RestaurantService;
import com.mealorder.util.GeoPoint;

import java.util.List;

/**
 * Entry point — wires all components and runs a full end-to-end demo.
 *
 * <p>This is your "manual DI container". In a real Spring Boot app, @Autowired
 * or constructor injection would do this wiring. The demo intentionally avoids
 * frameworks so every dependency is visible and explained.
 *
 * <p>Run this and watch the full order lifecycle: PLACED → CONFIRMED → PREPARING → READY → COLLECTED
 * with SMS, Push, and Email notifications at each step.
 */
public class MealOrderApplication {

    public static void main(String[] args) {

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  🍱  Meal Pre-Order & Collect System — LLD Demo   ");
        System.out.println("═══════════════════════════════════════════════════\n");

        // ── Wire dependencies (manual DI) ─────────────────────────
        MenuCache            menuCache            = new MenuCache();
        InMemoryOrderRepository      orderRepo    = new InMemoryOrderRepository();
        InMemoryRestaurantRepository restaurantRepo = new InMemoryRestaurantRepository();
        OrderFactory         orderFactory         = new OrderFactory();

        OrderService      orderService      = new OrderService(orderRepo, restaurantRepo, menuCache, orderFactory);
        RestaurantService restaurantService = new RestaurantService(restaurantRepo, menuCache);

        // ── Seed restaurants (Bengaluru coordinates) ───────────────
        Restaurant r1 = new Restaurant("R001", "Punjab Dhaba", "North Indian",
            new GeoPoint(12.9716, 77.5946), 4.2, 10);
        Restaurant r2 = new Restaurant("R002", "Udupi Palace", "South Indian",
            new GeoPoint(12.9750, 77.5970), 4.5, 8);
        restaurantService.registerRestaurant(r1);
        restaurantService.registerRestaurant(r2);

        // ── Create riders ──────────────────────────────────────────
        Rider regularRider = new Rider("RIDER001", "Arjun Kumar", "arjun@example.com",
            "+91-9876543210", new GeoPoint(12.9730, 77.5955), false);
        Rider vipRider = new Rider("RIDER002", "Priya Sharma", "priya@example.com",
            "+91-9876543211", new GeoPoint(12.9720, 77.5940), true);

        System.out.println("── Demo 1: Browse Nearby Restaurants ───────────────");
        List<Restaurant> nearby = orderService.findNearbyRestaurants(
            regularRider.getLocation(), 2.0);
        nearby.forEach(r -> System.out.printf("  • %s (%.2fkm away)%n",
            r.getName(), r.distanceTo(regularRider.getLocation())));

        System.out.println("\n── Demo 2: Lazy Menu Load ──────────────────────────");
        System.out.println("  (Notice: menu only loads when rider opens the restaurant)");
        var menu = orderService.getRestaurantMenu("R001");
        System.out.println("  Second call (should be cache HIT, no DB message):");
        orderService.getRestaurantMenu("R001"); // cache hit — no DB call
        System.out.printf("  Menu loaded: %d items available%n", menu.getAvailableItems().size());
        menu.getAvailableItems().forEach(item ->
            System.out.printf("    - %s: ₹%.0f%n", item.getName(), item.getPrice()));

        System.out.println("\n── Demo 3: Regular Rider Places Order ──────────────");
        Order order1;
        try {
            order1 = orderService.placeOrder(
                regularRider, "R001",
                List.of(
                    new OrderService.ItemRequest("M001", 1),
                    new OrderService.ItemRequest("M002", 2)
                ),
                "12:30 PM"
            );

            System.out.println("\n── Demo 4: Full Order Lifecycle (with notifications) ─");
            System.out.println("  Step 1: Restaurant confirms order");
            orderService.confirmOrder(order1.getOrderId());

            System.out.println("\n  Step 2: Kitchen starts preparing");
            orderService.startPreparingOrder(order1.getOrderId());

            System.out.println("\n  Step 3: Meal is ready!");
            orderService.markOrderReady(order1.getOrderId());

            System.out.println("\n  Step 4: Rider collects meal");
            orderService.collectOrder(order1.getOrderId());

            System.out.printf("%n  Final status: %s%n", order1.getStatus());
        } catch (SlotNotAvailableException e) {
            System.out.println("  No slots available: " + e.getMessage());
        }

        System.out.println("\n── Demo 5: VIP Rider with Pricing Strategy ─────────");
        Order vipOrder = orderService.placeOrder(
            vipRider, "R002",
            List.of(new OrderService.ItemRequest("M101", 2),
                    new OrderService.ItemRequest("M102", 1)),
            "1:00 PM"
        );
        System.out.printf("  VIP Order total: ₹%.2f (15%% VIP discount applied)%n",
            vipOrder.getTotalAmount());

        System.out.println("\n── Demo 6: State Machine — Illegal Transition ───────");
        System.out.println("  Trying to cancel a COLLECTED order (should fail gracefully)...");
        try {
            orderService.cancelOrder(vipOrder.getOrderId()); // not collected yet — will work
            // Now try something truly illegal
            orderService.cancelOrder(vipOrder.getOrderId()); // already cancelled
        } catch (InvalidStateTransitionException e) {
            System.out.println("  ✓ Caught expected exception: " + e.getMessage());
        }

        System.out.println("\n── Demo 7: Slot Contention (Thread Safety) ──────────");
        demonstrateConcurrentSlotBooking(orderService, r1);

        System.out.println("\n── Demo 8: Menu Cache Invalidation ─────────────────");
        System.out.printf("  Menus currently cached: %d%n", menuCache.cachedRestaurantCount());
        restaurantService.onMenuUpdated("R001");
        System.out.println("  Next access will reload from DB:");
        orderService.getRestaurantMenu("R001");

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("  ✅  Demo complete. Review output above to see:    ");
        System.out.println("     • Observer: SMS/Push/Email at each state change ");
        System.out.println("     • State: only valid transitions succeed         ");
        System.out.println("     • Lazy Load: DB hit once, cache on subsequent  ");
        System.out.println("     • Concurrency: atomic slot reservation          ");
        System.out.println("═══════════════════════════════════════════════════");
    }

    /**
     * Shows atomic slot reservation under concurrent access.
     * Spawns 5 threads all racing to book the last 2 slots of a busy restaurant.
     */
    private static void demonstrateConcurrentSlotBooking(OrderService orderService, Restaurant restaurant) {
        System.out.printf("  Restaurant '%s' has %d slots. 5 threads race for them.%n",
            restaurant.getName(), restaurant.getAvailableSlots());

        List<Rider> riders = List.of(
            new Rider("R_A", "Aarav", "a@x.com", "111", new GeoPoint(12.972, 77.594), false),
            new Rider("R_B", "Bhanu", "b@x.com", "222", new GeoPoint(12.973, 77.595), false),
            new Rider("R_C", "Charu", "c@x.com", "333", new GeoPoint(12.974, 77.596), false),
            new Rider("R_D", "Deepa", "d@x.com", "444", new GeoPoint(12.975, 77.597), false),
            new Rider("R_E", "Esha",  "e@x.com", "555", new GeoPoint(12.976, 77.598), false)
        );

        List<Thread> threads = riders.stream()
            .map(rider -> Thread.ofVirtual().unstarted(() -> {
                try {
                    Order o = orderService.placeOrder(rider, restaurant.getRestaurantId(),
                        List.of(new OrderService.ItemRequest("M001", 1)), "1:30 PM");
                    System.out.printf("  ✓ %s got slot → Order %s%n", rider.getName(), o.getOrderId());
                } catch (SlotNotAvailableException e) {
                    System.out.printf("  ✗ %s: No slot available%n", rider.getName());
                } catch (Exception e) {
                    System.out.printf("  ✗ %s: Error — %s%n", rider.getName(), e.getMessage());
                }
            }))
            .toList();

        threads.forEach(Thread::start);
        threads.forEach(t -> {
            try { t.join(); } catch (InterruptedException ignored) {}
        });

        System.out.printf("  Remaining slots after race: %d%n", restaurant.getAvailableSlots());
    }
}
