package com.mealorder.repository;

import com.mealorder.model.Restaurant;
import com.mealorder.util.GeoPoint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link RestaurantRepository}.
 * Uses linear scan for nearby search — replace with geospatial index in production.
 */
public class InMemoryRestaurantRepository implements RestaurantRepository {

    private final ConcurrentHashMap<String, Restaurant> store = new ConcurrentHashMap<>();

    @Override
    public void save(Restaurant restaurant) {
        store.put(restaurant.getRestaurantId(), restaurant);
    }

    @Override
    public Optional<Restaurant> findById(String restaurantId) {
        return Optional.ofNullable(store.get(restaurantId));
    }

    @Override
    public List<Restaurant> findNearby(GeoPoint center, double radiusKm) {
        return store.values().stream()
            .filter(r -> r.distanceTo(center) <= radiusKm)
            .sorted(Comparator.comparingDouble(r -> r.distanceTo(center)))
            .collect(Collectors.toList());
    }
}
