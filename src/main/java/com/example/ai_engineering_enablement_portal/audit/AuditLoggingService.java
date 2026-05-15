package com.example.ai_engineering_enablement_portal.audit;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class AuditLoggingService {
    private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();

    public void record(UUID taskId, String action, Map<String, Object> details) {
        events.add(AuditEvent.now(taskId, action, details));
    }

    public List<AuditEvent> eventsFor(UUID taskId) {
        return events.stream()
                .filter(event -> event.taskId().equals(taskId))
                .toList();
    }
}
