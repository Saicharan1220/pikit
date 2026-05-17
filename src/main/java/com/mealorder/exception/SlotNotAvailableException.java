package com.mealorder.exception;

/**
 * Thrown when a rider tries to book a time slot that is already full.
 *
 * <p><b>Concurrency note:</b> Even after an availability check returns true,
 * the slot can be taken by another thread before the booking completes.
 * This exception handles that race condition gracefully — callers retry or
 * show "slot just filled" to the user.
 */
public class SlotNotAvailableException extends RuntimeException {

    private final String restaurantId;
    private final String slotTime;

    public SlotNotAvailableException(String restaurantId, String slotTime) {
        super(String.format("No slots available at restaurant %s for slot %s", restaurantId, slotTime));
        this.restaurantId = restaurantId;
        this.slotTime = slotTime;
    }

    public String getRestaurantId() { return restaurantId; }
    public String getSlotTime()     { return slotTime;     }
}
