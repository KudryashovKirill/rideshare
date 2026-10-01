package com.example.rideshare.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Метаданные каждого события это его паспорт, которая сопровождает payload.
 */
public record EventMetadata(
        String eventId,
        String correlationId,
        Instant timestamp,
        String source,
        String eventType
) {
    /**
     * Фабричный метод для создания метаданных на стороне publisher'а.
     * Генерирует UUID и ставит текущее время автоматически.
     */
    public static EventMetadata create(String source, String eventType, String correlationId) {
        return new EventMetadata(
                UUID.randomUUID().toString(),
                correlationId != null ? correlationId : UUID.randomUUID().toString(),
                Instant.now(),
                source,
                eventType
        );
    }
}

