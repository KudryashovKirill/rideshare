package com.example.rideshare_rest.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/operator")
public class DiagnosticController {

    @GetMapping("/diagnostics")
    @PreAuthorize("hasRole('OPERATOR')")
    public Map<String, Object> getDiagnostics() {
        return Map.of(
                "timestamp", Instant.now().toString()
        );
    }
}
