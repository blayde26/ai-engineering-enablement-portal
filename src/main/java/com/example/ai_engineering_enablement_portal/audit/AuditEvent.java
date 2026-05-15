package com.example.ai_engineering_enablement_portal.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEvent(
        UUID taskId,
        String action,
        Map<String, Object> details,
        Instant createdAt) {

    public static AuditEvent now(UUID taskId, String action, Map<String, Object> details) {
        return new AuditEvent(taskId, action, details, Instant.now());
    }
}
