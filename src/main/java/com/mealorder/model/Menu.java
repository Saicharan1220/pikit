package com.mealorder.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A restaurant's full menu — a collection of {@link MenuItem}s.
 *
 * <p><b>Lazy Loading context:</b> A {@code Menu} object is expensive to build
 * (requires a DB round-trip). {@link com.mealorder.model.Restaurant} does NOT
 * create a Menu in its constructor — it defers creation until the first call to
 * {@code getMenu()}, via {@link com.mealorder.cache.MenuCache}.
 *
 * <p>Once loaded, the Menu is cached — subsequent reads cost nothing.
 */
public class Menu {

    private final String restaurantId;
    // LinkedHashMap preserves insertion order — menus display in add-order
    private final Map<String, MenuItem> items;

    public Menu(String restaurantId) {
        this.restaurantId = restaurantId;
        this.items = new LinkedHashMap<>();
    }

    /** Adds or replaces an item. Use when a restaurant updates their menu. */
    public void addItem(MenuItem item) {
        items.put(item.getItemId(), item);
    }

    public Optional<MenuItem> findItem(String itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    /** Returns only items currently marked available. */
    public List<MenuItem> getAvailableItems() {
        return items.values().stream()
            .filter(MenuItem::isAvailable)
            .collect(Collectors.toUnmodifiableList());
    }

    public List<MenuItem> getAllItems() {
        return Collections.unmodifiableList(List.copyOf(items.values()));
    }

    public String getRestaurantId() { return restaurantId; }
    public int    size()            { return items.size(); }
}
