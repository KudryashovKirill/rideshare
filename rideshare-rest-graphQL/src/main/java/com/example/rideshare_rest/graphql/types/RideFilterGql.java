package com.example.rideshare_rest.graphql.types;

/**
 * Входной тип для фильтрации поездок.
 * Соответствует input RideFilter в GraphQL-схеме.
 * <p>
 * Все поля необязательны — клиент передаёт только нужные фильтры.
 */
public record RideFilterGql(
        String driverId,
        String departureCity,
        String arrivalCity,
        Integer freeSeats
) {
}
