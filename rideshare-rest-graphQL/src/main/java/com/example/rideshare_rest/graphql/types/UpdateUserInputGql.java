package com.example.rideshare_rest.graphql.types;

import java.time.LocalDate;

/**
 * Входной тип для обновления пользователя.
 * Соответствует input UpdateUserInput в GraphQL-схеме.
 */
public record UpdateUserInputGql(
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate
) {
}
