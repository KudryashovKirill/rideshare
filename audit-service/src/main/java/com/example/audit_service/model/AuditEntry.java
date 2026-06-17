package com.example.audit_service.model;

import java.time.Instant;

/**
 * Запись в журнале аудита.
 *
 */
public record AuditEntry(
        long sequenceNumber,

        String eventId,

        String eventType,

        String source,

        String correlationId,

        Instant eventTimestamp,

        Instant receivedAt,

        String description
){
}
