package com.example.rideshare.events;

/**
 * Обёртка для любого доменного события.
 * <p>
 *
 * @param <T> тип полезной нагрузки (com.example.rideshare.events.RideEvent.Created, com.example.rideshare.events.UserEvent.Deleted и т.д.)
 */
public record EventEnvelope<T>(
        EventMetadata eventMetadata,
        T payload
) {
    public static <T> EventEnvelope<T> wrap(T payload, String source, String eventType, String correlationId) {
        return new EventEnvelope<>(
                EventMetadata.create(source, eventType, correlationId),
                payload
        );
    }
}
