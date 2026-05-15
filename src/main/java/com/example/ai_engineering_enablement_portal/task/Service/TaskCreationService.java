package com.example.ai_engineering_enablement_portal.task.Service;

import com.example.ai_engineering_enablement_portal.audit.AuditLoggingService;
import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Exception.TaskConflictException;
import com.example.ai_engineering_enablement_portal.task.Repository.TaskRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TaskCreationService {
    private final TaskRepository taskRepository;
    private final AuditLoggingService auditLoggingService;

    public TaskCreationService(TaskRepository taskRepository, AuditLoggingService auditLoggingService) {
        this.taskRepository = taskRepository;
        this.auditLoggingService = auditLoggingService;
    }

    public AiTask create(UUID taskId, Map<String, Object> payload) {
        AiTask task = new AiTask(taskId, payload);
        AiTask created = taskRepository.create(task)
                .orElseThrow(() -> new TaskConflictException("Task already exists"));
        auditLoggingService.record(taskId, "task.created", Map.of("status", created.taskStatus().value()));
        return created;
    }
}
