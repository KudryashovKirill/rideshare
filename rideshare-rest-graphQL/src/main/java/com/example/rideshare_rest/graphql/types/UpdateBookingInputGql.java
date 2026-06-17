package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.BookingStatus;

/**
 * Входной тип для обновления бронирования.
 * Соответствует input UpdateBookingInput в GraphQL-схеме.
 */
public record UpdateBookingInputGql(
        BookingStatus status,
        Integer requestedSeats
) {
}
