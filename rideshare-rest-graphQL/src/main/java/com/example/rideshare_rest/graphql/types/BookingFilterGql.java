package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.BookingStatus;

/**
 * Входной тип для фильтрации бронирований.
 * Соответствует input BookingFilter в GraphQL-схеме.
 *
 * Все поля необязательны — клиент передаёт только нужные фильтры.
 */
public record BookingFilterGql(
        BookingStatus status
) {
}
