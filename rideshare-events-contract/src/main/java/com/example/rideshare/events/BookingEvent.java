package com.example.rideshare.events;

public sealed interface BookingEvent {
    record Created(
            Long id,
            String rideId,
            String passengerId,
            String status,
            Integer requestedSeats
    ) implements BookingEvent {
    }

    record Updated(
            Long id,
            String status,
            Integer requestedSeats
    ) implements BookingEvent {
    }

    record Deleted(
            Long id,
            String status
    ) implements BookingEvent {
    }
}
