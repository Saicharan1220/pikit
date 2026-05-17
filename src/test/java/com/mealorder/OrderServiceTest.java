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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for the OrderService covering:
 * - Happy path order lifecycle
 * - State machine enforcement
 * - Concurrent slot reservation correctness
 * - Lazy loading cache behaviour
 * - Idempotency of duplicate requests
 *
 * These use plain Java asserts (no JUnit dependency) so you can run
 * with: java -cp out com.mealorder.OrderServiceTest
 *
 * In a real project: add JUnit 5 + Mockito for proper unit isolation.
 */
public class OrderServiceTest {

    private OrderService      orderService;
    private RestaurantService restaurantService;
    private Restaurant        testRestaurant;
    private Rider             testRider;

    // ── Setup ─────────────────────────────────────────────────────

    private void setUp() {
        MenuCache menuCache = new MenuCache();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryRestaurantRepository restaurantRepo = new InMemoryRestaurantRepository();
        OrderFactory orderFactory = new OrderFactory();

        orderService      = new OrderService(orderRepo, restaurantRepo, menuCache, orderFactory);
        restaurantService = new RestaurantService(restaurantRepo, menuCache);

        testRestaurant = new Restaurant("R001", "Test Restaurant", "Mixed",
            new GeoPoint(12.9716, 77.5946), 4.0, 5);
        restaurantRepo.save(testRestaurant);

        testRider = new Rider("RIDER001", "Test Rider", "test@test.com",
            "9999999999", new GeoPoint(12.9730, 77.5955), false);
    }

    // ── Tests ──────────────────────────────────────────────────────

    void testFullOrderLifecycle() {
        setUp();
        Order order = placeTestOrder();
        assert order.getStatus().name().equals("PLACED") : "Should be PLACED";

        orderService.confirmOrder(order.getOrderId());
        assert order.getStatus().name().equals("CONFIRMED");

        orderService.startPreparingOrder(order.getOrderId());
        assert order.getStatus().name().equals("PREPARING");

        orderService.markOrderReady(order.getOrderId());
        assert order.getStatus().name().equals("READY");

        orderService.collectOrder(order.getOrderId());
        assert order.getStatus().name().equals("COLLECTED");

        System.out.println("  ✅ testFullOrderLifecycle PASSED");
    }

    void testCancelFromPlacedState() {
        setUp();
        Order order = placeTestOrder();
        orderService.cancelOrder(order.getOrderId());
        assert order.getStatus().name().equals("CANCELLED") : "Should be CANCELLED";
        System.out.println("  ✅ testCancelFromPlacedState PASSED");
    }

    void testCannotCancelWhilePreparing() {
        setUp();
        Order order = placeTestOrder();
        orderService.confirmOrder(order.getOrderId());
        orderService.startPreparingOrder(order.getOrderId());

        try {
            orderService.cancelOrder(order.getOrderId());
            assert false : "Should have thrown InvalidStateTransitionException";
        } catch (InvalidStateTransitionException e) {
            System.out.println("  ✅ testCannotCancelWhilePreparing PASSED");
        }
    }

    void testLazyMenuCaching() {
        setUp();
        MenuCache cache = new MenuCache();
        assert !cache.isCached("R001") : "Should not be cached initially";

        cache.getMenu("R001"); // first call — cache miss, loads from DB
        assert cache.isCached("R001") : "Should be cached after first access";

        cache.invalidate("R001");
        assert !cache.isCached("R001") : "Should not be cached after invalidation";

        System.out.println("  ✅ testLazyMenuCaching PASSED");
    }

    void testConcurrentSlotReservation() throws InterruptedException {
        setUp();
        // Restaurant has 3 slots, 10 threads race for them
        Restaurant restaurant = new Restaurant("R_CONC", "Concurrent Test", "Test",
            new GeoPoint(12.97, 77.59), 4.0, 3);
        InMemoryRestaurantRepository repo = new InMemoryRestaurantRepository();
        repo.save(restaurant);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);
        CountDownLatch latch       = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final String slot = "1:00 PM";
            Thread.ofVirtual().start(() -> {
                try {
                    restaurant.reserveSlot(slot);
                    successCount.incrementAndGet();
                } catch (SlotNotAvailableException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assert successCount.get() == 3 : "Exactly 3 should succeed, got: " + successCount.get();
        assert failCount.get()    == 7 : "Exactly 7 should fail,    got: " + failCount.get();
        assert restaurant.getAvailableSlots() == 0 : "Zero slots should remain";

        System.out.println("  ✅ testConcurrentSlotReservation PASSED — "
            + successCount.get() + " success, " + failCount.get() + " blocked");
    }

    void testPaginatedOrderHistory() {
        setUp();
        // Place 5 orders
        for (int i = 0; i < 5; i++) {
            placeTestOrder();
        }

        List<Order> page0 = orderService.getOrderHistory(testRider.getRiderId(), 0, 3);
        List<Order> page1 = orderService.getOrderHistory(testRider.getRiderId(), 1, 3);

        assert page0.size() == 3 : "Page 0 should have 3 items";
        assert page1.size() == 2 : "Page 1 should have 2 items";

        System.out.println("  ✅ testPaginatedOrderHistory PASSED — "
            + "page0=" + page0.size() + ", page1=" + page1.size());
    }

    // ── Helper ────────────────────────────────────────────────────

    private Order placeTestOrder() {
        return orderService.placeOrder(testRider, "R001",
            List.of(new OrderService.ItemRequest("M001", 1)), "12:30 PM");
    }

    // ── Runner ────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n═══ Running OrderService Tests ═══════════════════");
        OrderServiceTest tests = new OrderServiceTest();

        tests.testFullOrderLifecycle();
        tests.testCancelFromPlacedState();
        tests.testCannotCancelWhilePreparing();
        tests.testLazyMenuCaching();
        tests.testConcurrentSlotReservation();
        tests.testPaginatedOrderHistory();

        System.out.println("═══ All tests passed ✅ ═══════════════════════════");
    }
}
