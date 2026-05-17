package com.mealorder.repository;

import com.mealorder.enums.OrderStatus;
import com.mealorder.exception.OrderNotFoundException;
import com.mealorder.model.Order;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link OrderRepository}.
 *
 * <p>Used in tests and the local demo. Swap for a Spring Data JPA
 * or MyBatis implementation to connect a real database.
 *
 * <p><b>Thread Safety:</b> {@code ConcurrentHashMap} instead of {@code HashMap}
 * because multiple order service threads may read/write concurrently.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<String, Order>       store          = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String>      idempotencyIndex = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        // Idempotency check — prevents duplicate orders if client retries
        if (idempotencyIndex.containsKey(order.getIdempotencyKey())) {
            System.out.printf("[Repo] Duplicate request — idempotency key %s already processed%n",
                order.getIdempotencyKey());
            return;
        }
        store.put(order.getOrderId(), order);
        idempotencyIndex.put(order.getIdempotencyKey(), order.getOrderId());
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<Order> findActiveOrdersByRider(String riderId) {
        Set<OrderStatus> terminal = EnumSet.of(OrderStatus.COLLECTED, OrderStatus.CANCELLED);
        return store.values().stream()
            .filter(o -> o.getRiderId().equals(riderId) && !terminal.contains(o.getStatus()))
            .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findOrderHistoryByRider(String riderId, int page, int size) {
        // Simulate pagination — in SQL: SELECT ... LIMIT size OFFSET page*size
        return store.values().stream()
            .filter(o -> o.getRiderId().equals(riderId))
            .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
            .skip((long) page * size)
            .limit(size)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByRestaurantAndStatus(String restaurantId, OrderStatus status) {
        return store.values().stream()
            .filter(o -> o.getRestaurantId().equals(restaurantId) && o.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public void update(Order order) {
        if (!store.containsKey(order.getOrderId())) {
            throw new OrderNotFoundException(order.getOrderId());
        }
        store.put(order.getOrderId(), order);
    }
}
