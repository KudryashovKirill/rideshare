package com.example.rideshare.events;

import java.util.UUID;

public sealed interface BookingEvent {
    record Created(
            UUID id,
            String rideId,
            String passengerId,
            String status,
            Integer requestedSeats
    ) implements BookingEvent {
    }

    record Updated(
            UUID id,
            String status,
            Integer requestedSeats
    ) implements BookingEvent {
    }

    record Deleted(
            UUID id,
            String status
    ) implements BookingEvent {
    }
}
