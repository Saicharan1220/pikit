package com.mealorder.util;

/**
 * Immutable value object representing a geographic coordinate.
 *
 * <p><b>Design note:</b> This is a Value Object (DDD term) — two GeoPoints
 * with the same lat/lng are considered equal regardless of object identity.
 * That's why we override {@code equals()} and {@code hashCode()}.
 *
 * <p><b>Resume talking point:</b> Making domain primitives first-class objects
 * (instead of passing raw doubles) prevents lat/lng parameter swap bugs at compile time.
 */
public final class GeoPoint {

    private final double latitude;
    private final double longitude;

    public GeoPoint(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Haversine formula — great-circle distance between two points on Earth.
     * Returns distance in kilometres.
     *
     * <p><b>Interview note:</b> For production, use PostGIS or Redis GEOSEARCH
     * instead of computing this in Java for every pair — those use spatial indexes.
     */
    public double distanceTo(GeoPoint other) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double getLatitude()  { return latitude;  }
    public double getLongitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPoint g)) return false;
        return Double.compare(g.latitude, latitude) == 0
            && Double.compare(g.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("GeoPoint(%.4f, %.4f)", latitude, longitude);
    }
}
