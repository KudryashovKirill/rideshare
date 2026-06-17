package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.BookingResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком бронирований.
 * Соответствует типу BookingConnection в GraphQL-схеме.
 */
public record BookingConnectionGql(
        List<BookingResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {
}
