package com.mealorder.model;

import com.mealorder.enums.OrderStatus;
import com.mealorder.enums.OrderType;
import com.mealorder.observer.OrderObserver;
import com.mealorder.state.OrderStateHandler;
import com.mealorder.state.PlacedState;
import com.mealorder.strategy.PricingStrategy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core domain entity representing a meal order from placement to collection.
 *
 * <p><b>Patterns used:</b>
 * <ul>
 *   <li><b>State Pattern</b> — delegates all lifecycle actions to the current
 *       {@link OrderStateHandler}, which enforces valid transitions.
 *   <li><b>Observer Pattern</b> — broadcasts every state change to registered
 *       {@link OrderObserver}s (SMS, Push, Email). The Order knows nothing about
 *       notification channels.
 *   <li><b>Strategy Pattern</b> — pricing is injected; the Order does not
 *       contain any pricing logic.
 *   <li><b>Builder Pattern</b> — construction is explicit and validated;
 *       no 7-argument constructor.
 * </ul>
 *
 * <p><b>Thread safety:</b>
 * <ul>
 *   <li>{@code CopyOnWriteArrayList} for observers — safe to iterate while
 *       another thread adds/removes observers.
 *   <li>{@code synchronized} on {@code updateStatus()} — prevents two threads
 *       from transitioning the same order simultaneously.
 * </ul>
 */
public class Order {

    private final String          orderId;
    private final String          riderId;
    private final String          restaurantId;
    private final OrderType       orderType;
    private final List<OrderItem> items;
    private final double          totalAmount;
    private final String          slotTime;
    private final LocalDateTime   createdAt;
    private final String          idempotencyKey; // prevents duplicate orders on retry

    // Mutable state — guarded by synchronized methods
    private volatile OrderStatus      status;
    private volatile OrderStateHandler stateHandler;

    /**
     * CopyOnWriteArrayList: safe for concurrent reads (iteration during broadcast)
     * and infrequent writes (observer registration). Perfect for this use case.
     *
     * Resume talking point: choosing the right concurrent collection for the
     * access pattern — not just defaulting to synchronized ArrayList everywhere.
     */
    private final List<OrderObserver> observers = new CopyOnWriteArrayList<>();

    private Order(Builder builder) {
        this.orderId        = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.riderId        = builder.riderId;
        this.restaurantId   = builder.restaurantId;
        this.orderType      = builder.orderType;
        this.items          = Collections.unmodifiableList(builder.items);
        this.totalAmount    = builder.pricingStrategy.calculateTotal(builder.items);
        this.slotTime       = builder.slotTime;
        this.createdAt      = LocalDateTime.now();
        this.idempotencyKey = builder.idempotencyKey;
        this.status         = OrderStatus.PLACED;
        this.stateHandler   = new PlacedState();
    }

    // ── Observer Management ─────────────────────────────────────────

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    /** Notifies all registered observers of the status change. */
    private void notifyObservers(OrderStatus newStatus) {
        // CopyOnWriteArrayList snapshot — safe even if observer removes itself during iteration
        for (OrderObserver observer : observers) {
            try {
                observer.onOrderStatusChanged(this, newStatus);
            } catch (Exception e) {
                // One failing notifier must not block others
                System.err.printf("[WARNING] Observer %s failed for order %s: %s%n",
                    observer.getClass().getSimpleName(), orderId, e.getMessage());
            }
        }
    }

    // ── State Transitions (delegate to current StateHandler) ────────

    public synchronized void confirm()       { stateHandler.confirm(this);       }
    public synchronized void startPreparing(){ stateHandler.startPreparing(this);}
    public synchronized void markReady()     { stateHandler.markReady(this);     }
    public synchronized void collect()       { stateHandler.collect(this);       }
    public synchronized void cancel()        { stateHandler.cancel(this);        }

    // ── Internal state mutators (called only by StateHandler) ────────

    /** Package-private: only StateHandler implementations call this. */
    public void setStatus(OrderStatus newStatus) {
        this.status = newStatus;
        notifyObservers(newStatus);
    }

    public void setStateHandler(OrderStateHandler handler) {
        this.stateHandler = handler;
    }

    // ── Getters ──────────────────────────────────────────────────────

    public String          getOrderId()        { return orderId;        }
    public String          getRiderId()        { return riderId;        }
    public String          getRestaurantId()   { return restaurantId;   }
    public OrderType       getOrderType()      { return orderType;      }
    public List<OrderItem> getItems()          { return items;          }
    public double          getTotalAmount()    { return totalAmount;    }
    public String          getSlotTime()       { return slotTime;       }
    public LocalDateTime   getCreatedAt()      { return createdAt;      }
    public OrderStatus     getStatus()         { return status;         }
    public String          getIdempotencyKey() { return idempotencyKey; }

    @Override
    public String toString() {
        return String.format("Order{id=%s, rider=%s, restaurant=%s, status=%s, total=₹%.2f}",
            orderId, riderId, restaurantId, status, totalAmount);
    }

    // ── Builder ──────────────────────────────────────────────────────

    /**
     * Builder for constructing an Order with validation.
     *
     * <p><b>Why Builder?</b> Orders have 6+ required fields. A constructor
     * with 6 parameters is error-prone (wrong argument order, invisible nulls).
     * Builder makes each field explicit and validates before construction.
     */
    public static class Builder {
        private String          riderId;
        private String          restaurantId;
        private OrderType       orderType       = OrderType.PRE_ORDER;
        private List<OrderItem> items;
        private PricingStrategy pricingStrategy;
        private String          slotTime;
        private String          idempotencyKey  = UUID.randomUUID().toString();

        public Builder riderId(String v)               { this.riderId = v;          return this; }
        public Builder restaurantId(String v)          { this.restaurantId = v;     return this; }
        public Builder orderType(OrderType v)          { this.orderType = v;        return this; }
        public Builder items(List<OrderItem> v)        { this.items = v;            return this; }
        public Builder pricingStrategy(PricingStrategy v){ this.pricingStrategy = v; return this; }
        public Builder slotTime(String v)              { this.slotTime = v;         return this; }
        public Builder idempotencyKey(String v)        { this.idempotencyKey = v;  return this; }

        public Order build() {
            if (riderId == null || riderId.isBlank())           throw new IllegalArgumentException("riderId required");
            if (restaurantId == null || restaurantId.isBlank()) throw new IllegalArgumentException("restaurantId required");
            if (items == null || items.isEmpty())               throw new IllegalArgumentException("Order must have at least one item");
            if (pricingStrategy == null)                        throw new IllegalArgumentException("pricingStrategy required");
            return new Order(this);
        }
    }
}
