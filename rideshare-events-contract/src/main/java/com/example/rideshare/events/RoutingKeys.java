package com.example.rideshare.events;

/**
 * Константы для маршрутизации событий в RabbitMQ.
 */

public final class RoutingKeys {
    private RoutingKeys() {
    }

    public static final String EXCHANGE = "rideshare.events";

    public static final String RIDE_CREATED = "ride.created";
    public static final String RIDE_UPDATED = "ride.updated";
    public static final String RIDE_DELETED = "ride.deleted";

    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_UPDATED = "booking.updated";
    public static final String BOOKING_DELETED = "booking.deleted";

    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";

    public static final String ALL_RIDE_EVENTS = "ride.*";
    public static final String ALL_BOOKING_EVENTS = "booking.*";
    public static final String ALL_USER_EVENTS = "user.*";
    public static final String ALL_EVENTS = "#";
}
