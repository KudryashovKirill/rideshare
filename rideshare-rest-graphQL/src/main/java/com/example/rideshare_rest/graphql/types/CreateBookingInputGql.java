package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.BookingStatus;

/**
 * Входной тип для создания бронирования.
 * Соответствует input CreateBookingInput в GraphQL-схеме.
 */
public record CreateBookingInputGql(
        String rideId,
        String passengerId,
        BookingStatus status,
        Integer requestedSeats
) {
}
