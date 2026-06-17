package com.example.rideshare.events;

import java.time.LocalDateTime;

public sealed interface RideEvent {
    record Created(
            Long id,
            Long driverId,
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
            Long id,
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
            Long id,
            Long driverId,
            String departureCity,
            String arrivalCity,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime
    ) implements RideEvent {
    }

    record Enriched(
            Long rideId,
            Integer estimatedDistanceKm,
            Integer recommendedPrice,
            String priceDeviation,
            String routeDifficulty
    ) implements RideEvent {
    }
}
