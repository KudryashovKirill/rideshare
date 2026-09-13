package com.example.rideshare.events;

import java.time.LocalDate;
import java.util.UUID;

public sealed interface UserEvent {
    record Created(
            UUID id,
            String firstName,
            String lastName,
            String fullName,
            String email,
            LocalDate birthDate
    ) implements UserEvent {
    }

    record Updated(
            UUID id,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate
    ) implements UserEvent {
    }

    record Deleted(
            UUID id,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate
    ) implements UserEvent {
    }
}
