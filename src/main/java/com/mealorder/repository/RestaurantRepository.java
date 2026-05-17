package com.mealorder.repository;

import com.mealorder.model.Restaurant;
import com.mealorder.util.GeoPoint;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Restaurant data access.
 */
public interface RestaurantRepository {

    void save(Restaurant restaurant);

    Optional<Restaurant> findById(String restaurantId);

    /**
     * Finds restaurants within a given radius of a location,
     * sorted by distance ascending.
     *
     * <p><b>Production note:</b> This would use a spatial index.
     * In PostgreSQL + PostGIS: {@code WHERE ST_DWithin(location, ?, radius)}
     * In Redis: {@code GEOSEARCH key FROMMEMBER rider BYRADIUS 5 km ASC}
     */
    List<Restaurant> findNearby(GeoPoint center, double radiusKm);
}
