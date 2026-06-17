package com.example.rideshare_rest.graphql.types;

import com.example.rideshare_api_contract.dto.UserResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком пользователей.
 * Соответствует типу UserConnection в GraphQL-схеме.
 */
public record UserConnectionGql(
        List<UserResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {
}
