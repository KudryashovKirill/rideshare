package com.example.rideshare_rest.graphql.types;

/**
 * Метаданные страницы для пагинации.
 * Соответствует типу PageInfo в GraphQL-схеме.
 */
public record PageInfoGql(
        int pageNumber,
        int pageSize,
        int totalPages,
        boolean last
) {
}
