package com.example.rideshare_rest.graphql.types;

import java.time.LocalDate;

/**
 * Входной тип для создания пользователя.
 * Соответствует input CreateUserInput в GraphQL-схеме.
 */
public record CreateUserInputGql(
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate
) {
}
