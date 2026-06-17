package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.RideStatus;

import java.time.LocalDateTime;

/**
 * Входной тип для обновления поездки.
 * Соответствует input UpdateRideInput в GraphQL-схеме.
 */
public record UpdateRideInputGql(
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
