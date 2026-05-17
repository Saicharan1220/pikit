package com.mealorder.service;

import com.mealorder.cache.MenuCache;
import com.mealorder.model.Restaurant;
import com.mealorder.repository.RestaurantRepository;
import com.mealorder.util.GeoPoint;

import java.util.List;

/**
 * Manages restaurant data and the menu invalidation lifecycle.
 *
 * <p>Separated from {@link OrderService} by SRP — restaurant management
 * and order management are distinct business capabilities.
 */
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCache            menuCache;

    public RestaurantService(RestaurantRepository restaurantRepository, MenuCache menuCache) {
        this.restaurantRepository = restaurantRepository;
        this.menuCache = menuCache;
    }

    public void registerRestaurant(Restaurant restaurant) {
        restaurantRepository.save(restaurant);
        System.out.printf("[RestaurantService] Registered: %s%n", restaurant);
    }

    public List<Restaurant> findNearby(GeoPoint location, double radiusKm) {
        return restaurantRepository.findNearby(location, radiusKm);
    }

    /**
     * Called when a restaurant updates their menu via the dashboard.
     * Evicts the old menu from cache — the next request triggers a fresh DB load.
     */
    public void onMenuUpdated(String restaurantId) {
        menuCache.invalidate(restaurantId);
        System.out.printf("[RestaurantService] Menu cache invalidated for %s%n", restaurantId);
    }
}
