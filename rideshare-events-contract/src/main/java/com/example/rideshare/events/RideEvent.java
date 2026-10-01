package com.example.rideshare.events;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface RideEvent {
    record Created(
            UUID id,
            UUID driverId,
            String departureCity,
            String arrivalCity,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Integer totalSeats,
            Integer freeSeats,
            String status,
            Integer price
    ) implements RideEvent {
    }

    record Updated(
            UUID id,
            String departureCity,
            String arrivalCity,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Integer totalSeats,
            Integer freeSeats,
            String status,
            Integer price
    ) implements RideEvent {
    }

    record Deleted(
            UUID id,
            UUID driverId,
            String departureCity,
            String arrivalCity,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime
    ) implements RideEvent {
    }

    record Enriched(
            UUID rideId,
            Integer estimatedDistanceKm,
            Integer recommendedPrice,
            String priceDeviation,
            String routeDifficulty
    ) implements RideEvent {
    }
}
