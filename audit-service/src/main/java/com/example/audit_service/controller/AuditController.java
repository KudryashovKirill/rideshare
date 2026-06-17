package com.example.audit_service.controller;

import com.example.audit_service.model.AuditEntry;
import com.example.audit_service.storage.AuditStorage;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditStorage auditStorage;

    public AuditController(AuditStorage auditStorage) {
        this.auditStorage = auditStorage;
    }

    /**
     * Возвращает последние аудит-записи.
     * <p>
     * Пример: GET /api/audit?limit=50
     */
    @GetMapping
    public Map<String, Object> getAuditLog(
            @RequestParam(defaultValue = "100") int limit) {

        List<AuditEntry> entries = auditStorage.findLatest(limit);

        return Map.of(
                "totalEntries", auditStorage.count(),
                "showing", entries.size(),
                "entries", entries
        );
    }

    @GetMapping("/{correlationId}")
    public Map<String, Object> getByCorrelationId(
            @PathVariable String correlationId) {
        List<AuditEntry> entries = auditStorage.findByCorrelationId(correlationId)
                .stream()
                .sorted((a, b) -> Long.compare(a.sequenceNumber(), b.sequenceNumber()))
                .toList();

        return Map.of(
                "totalEntries", auditStorage.count(),
                "showing", entries.size(),
                "entries", entries
        );
    }
}
