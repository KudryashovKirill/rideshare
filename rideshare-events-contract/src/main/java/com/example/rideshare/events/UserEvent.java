package com.example.rideshare.events;

import java.time.LocalDate;

public sealed interface UserEvent {
    record Created(
            Long id,
            String firstName,
            String lastName,
            String fullName,
            String email,
            LocalDate birthDate
    ) implements UserEvent {
    }

    record Updated(
            Long id,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate
    ) implements UserEvent {
    }

    record Deleted(
            Long id,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate
    ) implements UserEvent {
    }
}
