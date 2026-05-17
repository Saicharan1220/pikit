package com.mealorder.model;

import com.mealorder.util.GeoPoint;
import java.util.List;

/**
 * Represents a customer who places and collects meal orders.
 *
 * <p><b>Lazy Loading:</b> {@code orderHistory} is intentionally left as a
 * parameter to fetch methods rather than a stored field. The history is loaded
 * on demand from the repository — not on Rider construction.
 *
 * <p>In a Spring/Hibernate environment this maps to {@code FetchType.LAZY}
 * on a {@code @OneToMany} relationship. Here we model it explicitly.
 */
public class Rider {

    private final String   riderId;
    private final String   name;
    private final String   email;
    private final String   phone;
    private       GeoPoint location;
    private final boolean  isVip;

    public Rider(String riderId, String name, String email, String phone,
                 GeoPoint location, boolean isVip) {
        this.riderId   = riderId;
        this.name      = name;
        this.email     = email;
        this.phone     = phone;
        this.location  = location;
        this.isVip     = isVip;
    }

    /**
     * Updates the rider's current location.
     * In production: called periodically from the mobile app.
     * Used by the locker-matching algorithm to find the nearest pickup point.
     */
    public void updateLocation(GeoPoint newLocation) {
        this.location = newLocation;
    }

    public String   getRiderId()  { return riderId;  }
    public String   getName()     { return name;     }
    public String   getEmail()    { return email;    }
    public String   getPhone()    { return phone;    }
    public GeoPoint getLocation() { return location; }
    public boolean  isVip()       { return isVip;    }

    @Override
    public String toString() {
        return String.format("Rider{%s, %s, vip=%s}", riderId, name, isVip);
    }
}
