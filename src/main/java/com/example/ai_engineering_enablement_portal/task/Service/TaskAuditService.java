package com.example.ai_engineering_enablement_portal.task.Service;

import com.example.ai_engineering_enablement_portal.audit.AuditLoggingService;
import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.ReviewStatus;
import com.example.ai_engineering_enablement_portal.task.TaskReview;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TaskAuditService {
    private final TaskRetrievalService taskRetrievalService;
    private final AuditLoggingService auditLoggingService;

    public TaskAuditService(TaskRetrievalService taskRetrievalService, AuditLoggingService auditLoggingService) {
        this.taskRetrievalService = taskRetrievalService;
        this.auditLoggingService = auditLoggingService;
    }

    public AiTask audit(UUID taskId, ReviewStatus reviewStatus, String reviewComments) {
        AiTask task = taskRetrievalService.retrieve(taskId);
        task.recordReview(new TaskReview(reviewStatus, reviewComments, Instant.now()));
        auditLoggingService.record(taskId, "task.reviewed", Map.of(
                "review_status", reviewStatus.value(),
                "review_comments", reviewComments));
        return task;
    }
}
