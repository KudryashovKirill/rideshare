package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.RideStatus;

import java.time.LocalDateTime;

/**
 * Входной тип для создания поездки.
 * Соответствует input CreateRideInput в GraphQL-схеме.
 */
public record CreateRideInputGql(
        String driverId,
        String departureCity,
        String arrivalCity,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Integer totalSeats,
        Integer freeSeats,
        RideStatus status,
        Integer price
) {
}
