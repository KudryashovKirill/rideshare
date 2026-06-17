package com.example.audit_service.storage;

import com.example.audit_service.model.AuditEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AuditStorage {
    private final ConcurrentLinkedDeque<AuditEntry> entries = new ConcurrentLinkedDeque<>();
    private final AtomicLong sequence = new AtomicLong(0);

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public boolean isDuplicate(String eventId) {
        return !processedEventIds.add(eventId);
    }

    /**
     * Сохраняет аудит-запись. Присваивает порядковый номер, добавляет в начало списка.
     */
    public AuditEntry save(AuditEntry entry) {
        AuditEntry numbered = new AuditEntry(
                sequence.incrementAndGet(),
                entry.eventId(),
                entry.eventType(),
                entry.source(),
                entry.correlationId(),
                entry.eventTimestamp(),
                entry.receivedAt(),
                entry.description()
        );
        entries.addFirst(numbered);
        return numbered;
    }

    /**
     * Возвращает последние N записей (новые — первые).
     */
    public List<AuditEntry> findLatest(int limit) {
        return entries.stream()
                .limit(limit)
                .toList();
    }

    public List<AuditEntry> findByCorrelationId(String correlationId) {
        return entries.stream()
                .filter(entry -> entry.correlationId().equals(correlationId))
                .toList();
    }

    /**
     * Общее количество записей в журнале.
     */
    public int count() {
        return entries.size();
    }
}
