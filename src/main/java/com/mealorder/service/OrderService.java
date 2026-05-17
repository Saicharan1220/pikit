package com.mealorder.service;

import com.mealorder.cache.MenuCache;
import com.mealorder.enums.OrderStatus;
import com.mealorder.enums.OrderType;
import com.mealorder.exception.OrderNotFoundException;
import com.mealorder.exception.SlotNotAvailableException;
import com.mealorder.factory.OrderFactory;
import com.mealorder.model.Menu;
import com.mealorder.model.MenuItem;
import com.mealorder.model.Order;
import com.mealorder.model.OrderItem;
import com.mealorder.model.Restaurant;
import com.mealorder.model.Rider;
import com.mealorder.repository.OrderRepository;
import com.mealorder.repository.RestaurantRepository;
import com.mealorder.util.GeoPoint;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Orchestrates the core order placement and lifecycle management flow.
 *
 * <p><b>Design:</b> Service layer sits between the API/controller and the
 * domain model + repositories. It owns business logic that spans multiple
 * domain objects — e.g., "placing an order" touches Rider, Restaurant, Menu,
 * Order, and the repository in a single operation.
 *
 * <p><b>SRP:</b> OrderService does NOT handle notifications (that's Observer/NotificationService),
 * does NOT create Order objects (that's OrderFactory), and does NOT load menus
 * (that's MenuCache). It orchestrates their collaboration.
 *
 * <p><b>DIP:</b> Depends on repository and cache interfaces — not concrete implementations.
 * Test with in-memory fakes; deploy with real DB and Redis, zero service changes.
 */
public class OrderService {

    private final OrderRepository      orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCache            menuCache;
    private final OrderFactory         orderFactory;

    public OrderService(OrderRepository orderRepository,
                        RestaurantRepository restaurantRepository,
                        MenuCache menuCache,
                        OrderFactory orderFactory) {
        this.orderRepository      = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuCache            = menuCache;
        this.orderFactory         = orderFactory;
    }

    /**
     * Core use case: rider browses nearby restaurants.
     *
     * <p><b>Lazy Loading in action:</b> We return Restaurant objects but do NOT
     * load their menus. The menu is only fetched in {@link #getRestaurantMenu}.
     * If 20 restaurants are shown in a list, we skip 20 DB queries.
     */
    public List<Restaurant> findNearbyRestaurants(GeoPoint riderLocation, double radiusKm) {
        List<Restaurant> nearby = restaurantRepository.findNearby(riderLocation, radiusKm);
        System.out.printf("[OrderService] Found %d restaurants within %.1fkm%n",
            nearby.size(), radiusKm);
        return nearby;
    }

    /**
     * Rider opens a restaurant — NOW we load the menu (lazy load point).
     *
     * <p>First call: cache miss → DB load → cache stored.
     * Every subsequent call: cache hit → microsecond return.
     */
    public Menu getRestaurantMenu(String restaurantId) {
        return menuCache.getMenu(restaurantId);
    }

    /**
     * Places a new order. The core transactional operation.
     *
     * <p>Steps:
     * <ol>
     *   <li>Validate restaurant exists
     *   <li>Reserve a slot (atomic CAS — thread-safe)
     *   <li>Resolve menu items and build OrderItems
     *   <li>Create Order via factory (applies correct pricing + wires observers)
     *   <li>Persist (idempotency key prevents duplicates on retry)
     *   <li>Enqueue in kitchen queue
     * </ol>
     *
     * @param rider        the rider placing the order
     * @param restaurantId target restaurant
     * @param itemRequests list of (itemId, quantity) pairs
     * @param slotTime     requested pickup time
     * @return the placed Order in PLACED state
     */
    public Order placeOrder(Rider rider, String restaurantId,
                            List<ItemRequest> itemRequests, String slotTime) {

        // 1. Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));

        // 2. Reserve slot (throws SlotNotAvailableException if full)
        restaurant.reserveSlot(slotTime);

        // 3. Resolve menu items — load menu lazily and convert to OrderItems
        Menu menu = getRestaurantMenu(restaurantId);
        List<OrderItem> orderItems = itemRequests.stream()
            .map(req -> {
                MenuItem item = menu.findItem(req.getItemId())
                    .filter(MenuItem::isAvailable)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Item not available: " + req.getItemId()));
                return new OrderItem(item.getItemId(), item.getName(),
                                     req.getQuantity(), item.getPrice());
            })
            .collect(Collectors.toList());

        // 4. Create Order via factory (pricing + observers wired inside)
        Order order = orderFactory.createOrder(rider, restaurantId, orderItems,
                                               OrderType.PRE_ORDER, slotTime);

        // 5. Persist with idempotency
        orderRepository.save(order);

        // 6. Add to kitchen queue
        restaurant.enqueueOrder(order);

        System.out.printf("[OrderService] Order %s placed | slot: %s | total: ₹%.2f%n",
            order.getOrderId(), slotTime, order.getTotalAmount());

        return order;
    }

    /** Restaurant confirms the order. Triggers CONFIRMED notifications. */
    public void confirmOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        order.confirm();
        orderRepository.update(order);
    }

    /** Kitchen starts preparing. */
    public void startPreparingOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        order.startPreparing();
        orderRepository.update(order);
    }

    /** Meal is ready at the counter. Triggers READY SMS + push to rider. */
    public void markOrderReady(String orderId) {
        Order order = getOrderOrThrow(orderId);
        order.markReady();
        orderRepository.update(order);
    }

    /** Rider collects their meal. */
    public void collectOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        order.collect();
        orderRepository.update(order);
    }

    /**
     * Cancels an order and releases the reserved slot.
     *
     * <p>The State Pattern enforces that PREPARING / READY / COLLECTED orders
     * cannot be cancelled — no manual status check needed here.
     */
    public void cancelOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        String restaurantId = order.getRestaurantId();

        order.cancel(); // throws InvalidStateTransitionException if not cancellable
        orderRepository.update(order);

        // Release the slot so another rider can book it
        restaurantRepository.findById(restaurantId)
            .ifPresent(Restaurant::releaseSlot);
    }

    /** Paginated order history — lazy loading of historical data. */
    public List<Order> getOrderHistory(String riderId, int page, int size) {
        return orderRepository.findOrderHistoryByRider(riderId, page, size);
    }

    public Optional<Order> getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getPendingOrdersForRestaurant(String restaurantId) {
        return orderRepository.findByRestaurantAndStatus(restaurantId, OrderStatus.PLACED);
    }

    private Order getOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Simple value object for order item requests.
     * In a REST API this maps to the request body JSON.
     */
    public static class ItemRequest {
        private final String itemId;
        private final int    quantity;

        public ItemRequest(String itemId, int quantity) {
            this.itemId   = itemId;
            this.quantity = quantity;
        }

        public String getItemId()  { return itemId;   }
        public int    getQuantity(){ return quantity;  }
    }
}
