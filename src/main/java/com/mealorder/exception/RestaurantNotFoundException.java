package com.mealorder.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a restaurant is not found.
 */
public class RestaurantNotFoundException extends BaseException {

    public RestaurantNotFoundException(Long restaurantId) {
        super(
            "RESTAURANT_NOT_FOUND",
            String.format("Restaurant with ID %d not found", restaurantId),
            HttpStatus.NOT_FOUND
        );
    }
}
