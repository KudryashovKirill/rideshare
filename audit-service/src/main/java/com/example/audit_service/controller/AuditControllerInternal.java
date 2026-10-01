package com.example.audit_service.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class AuditControllerInternal {
    @GetMapping("/audit/rides/{rideId}")
    @PreAuthorize("hasRole('SERVICE')")
    public AuditResponse auditEvents(@PathVariable UUID rideId) {
        return new AuditResponse(rideId, List.of("RIDE_CREATED", "DRIVER_ASSIGNED", "PAYMENT_CONFIRMED"));
    }

    @GetMapping("/admin/info")
    @PreAuthorize("hasRole('AUDITOR')")
    public Map<String, String> info() {
        return Map.of("service", "audit-service", "status", "ok");
    }

    public record AuditResponse(UUID rideId, List<String> events) {}
}
