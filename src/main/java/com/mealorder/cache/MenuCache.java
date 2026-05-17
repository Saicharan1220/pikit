package com.mealorder.cache;

import com.mealorder.model.Menu;
import com.mealorder.model.MenuItem;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe lazy-loading cache for restaurant menus.
 *
 * <p><b>Pattern:</b> Cache-Aside (also called Lazy Loading at the infrastructure layer)
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>On first access for a restaurant, {@code getMenu()} calls the loader
 *       (simulated DB fetch here; Redis or real DB in production).
 *   <li>The result is stored in the ConcurrentHashMap.
 *   <li>Every subsequent access for the same restaurant is an O(1) in-memory read.
 * </ol>
 *
 * <p><b>Thread Safety:</b> {@code ConcurrentHashMap.computeIfAbsent()} is atomic —
 * even if 100 threads request the same restaurant's menu simultaneously, the DB
 * loader runs exactly once. No double-loading, no stale data races.
 *
 * <p><b>Resume talking point:</b> "How did you prevent the thundering herd problem
 * on cold cache?" → computeIfAbsent's internal striped locking ensures only one
 * thread loads per key; others wait and get the same result.
 *
 * <p><b>Production upgrade:</b> Replace this in-memory map with Redis.
 * Add a TTL (e.g., 10 minutes) so menus auto-expire when restaurants update them.
 * Add a {@code invalidate()} webhook endpoint that restaurants call when they
 * change their menu.
 */
public class MenuCache {

    /**
     * ConcurrentHashMap over HashMap: multiple threads read/write concurrently.
     * Synchronized HashMap would serialize all access — ConcurrentHashMap uses
     * 16 independent segment locks, allowing parallel reads with minimal contention.
     */
    private final ConcurrentHashMap<String, Menu> cache = new ConcurrentHashMap<>();

    /**
     * Returns the menu for the given restaurant, loading it if not yet cached.
     *
     * @param restaurantId the restaurant whose menu to fetch
     * @return the cached (or freshly loaded) menu
     */
    public Menu getMenu(String restaurantId) {
        return cache.computeIfAbsent(restaurantId, this::loadMenuFromDatabase);
    }

    /**
     * Evicts a restaurant's menu from cache.
     *
     * <p>Call this when a restaurant updates their menu. The next request
     * triggers a fresh DB load via the loader function.
     *
     * <p>In production: this would be called by a webhook handler whenever
     * the restaurant dashboard publishes a menu change event.
     */
    public void invalidate(String restaurantId) {
        cache.remove(restaurantId);
        System.out.printf("[MenuCache] Evicted menu for restaurant %s%n", restaurantId);
    }

    public boolean isCached(String restaurantId) {
        return cache.containsKey(restaurantId);
    }

    public int cachedRestaurantCount() {
        return cache.size();
    }

    /**
     * Simulates loading a menu from a database.
     *
     * <p>In production: execute a parameterized SQL query:
     * {@code SELECT * FROM menu_items WHERE restaurant_id = ? AND active = true}
     *
     * <p>The actual DB call is the expensive operation lazy loading defers.
     * When listing 20 nearby restaurants, we skip 20 calls like this.
     */
    private Menu loadMenuFromDatabase(String restaurantId) {
        System.out.printf("[MenuCache] Cache MISS — loading menu for restaurant %s from DB%n",
            restaurantId);

        // Simulated DB data
        Menu menu = new Menu(restaurantId);

        if ("R001".equals(restaurantId)) {
            menu.addItem(new MenuItem.Builder().itemId("M001").name("Butter Chicken").price(280).build());
            menu.addItem(new MenuItem.Builder().itemId("M002").name("Naan").price(40).build());
            menu.addItem(new MenuItem.Builder().itemId("M003").name("Dal Makhani").price(220).build());
        } else if ("R002".equals(restaurantId)) {
            menu.addItem(new MenuItem.Builder().itemId("M101").name("Masala Dosa").price(120).build());
            menu.addItem(new MenuItem.Builder().itemId("M102").name("Filter Coffee").price(60).build());
            menu.addItem(new MenuItem.Builder().itemId("M103").name("Idli Sambar").price(90).build());
        } else {
            menu.addItem(new MenuItem.Builder().itemId("M201").name("Veg Biryani").price(200).build());
        }

        System.out.printf("[MenuCache] Cache SET — menu for restaurant %s (%d items)%n",
            restaurantId, menu.size());
        return menu;
    }
}
