package com.mealorder.repository;

import com.mealorder.enums.OrderStatus;
import com.mealorder.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order persistence operations.
 *
 * <p><b>Pattern:</b> Repository (DDD / Martin Fowler)
 *
 * <p>The service layer depends on this interface, not on any concrete DB implementation.
 * This enables:
 * <ul>
 *   <li>Swapping PostgreSQL for DynamoDB with zero service layer changes
 *   <li>Using the in-memory implementation in tests without spinning up a DB
 *   <li>Mocking in unit tests — {@code when(repo.findById("X")).thenReturn(...)}
 * </ul>
 *
 * <p><b>SOLID:</b> Dependency Inversion Principle in action — the high-level
 * {@link com.mealorder.service.OrderService} depends on this abstraction,
 * not on JDBC, JPA, or any specific storage technology.
 */
public interface OrderRepository {

    /** Persists a new Order. Throws if the idempotency key already exists. */
    void save(Order order);

    /** Retrieves an Order by its ID. */
    Optional<Order> findById(String orderId);

    /** All active (non-terminal) orders for a given rider. */
    List<Order> findActiveOrdersByRider(String riderId);

    /**
     * Paginated order history for a rider, newest first.
     *
     * <p><b>Lazy Loading at the API layer:</b> We never load all orders at once.
     * {@code page=0, size=10} loads the first 10. The client requests more as
     * the user scrolls — this is cursor-based lazy loading.
     */
    List<Order> findOrderHistoryByRider(String riderId, int page, int size);

    /** All orders at a restaurant currently in a given status. */
    List<Order> findByRestaurantAndStatus(String restaurantId, OrderStatus status);

    /** Updates the persisted Order (called after status transitions). */
    void update(Order order);
}
