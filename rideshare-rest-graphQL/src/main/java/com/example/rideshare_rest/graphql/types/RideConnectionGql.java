package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.RideResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком поездок.
 * Соответствует типу RideConnection в GraphQL-схеме.
 */
public record RideConnectionGql(
        List<RideResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {
}
