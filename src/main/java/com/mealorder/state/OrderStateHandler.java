package com.mealorder.state;

import com.mealorder.model.Order;

/**
 * Defines the actions available on an Order at any given state.
 *
 * <p><b>Pattern:</b> State (GoF Behavioral)
 *
 * <p>Each concrete implementation allows only the transitions valid for its state
 * and throws {@link com.mealorder.exception.InvalidStateTransitionException}
 * for all others. This makes illegal state bugs impossible at runtime.
 *
 * <p><b>Why not a big if-else on OrderStatus enum?</b>
 * As states grow (add DELAYED, PARTIALLY_READY, etc.), if-else chains become
 * unmaintainable and error-prone. Each state class is independently testable
 * and follows the Open/Closed principle — add a state without touching others.
 */
public interface OrderStateHandler {

    /** Restaurant confirms they received and will fulfill the order. */
    void confirm(Order order);

    /** Kitchen starts preparing the meal. */
    void startPreparing(Order order);

    /** Meal is packaged and ready for rider collection. */
    void markReady(Order order);

    /** Rider physically collected the order. */
    void collect(Order order);

    /** Cancel the order (only valid before preparation begins). */
    void cancel(Order order);
}
