package com.example.ai_engineering_enablement_portal.task;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AiTask {
    private final UUID taskId;
    private final Map<String, Object> payload;
    private TaskStatus taskStatus;
    private Map<String, Object> result;
    private TaskReview review;
    private final Instant createdAt;
    private Instant updatedAt;

    public AiTask(UUID taskId, Map<String, Object> payload) {
        this.taskId = taskId;
        this.payload = new LinkedHashMap<>(payload);
        this.taskStatus = TaskStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID taskId() {
        return taskId;
    }

    public Map<String, Object> payload() {
        return Map.copyOf(payload);
    }

    public TaskStatus taskStatus() {
        return taskStatus;
    }

    public Map<String, Object> result() {
        return result == null ? null : Map.copyOf(result);
    }

    public TaskReview review() {
        return review;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void markExecuting() {
        this.taskStatus = TaskStatus.EXECUTING;
        touch();
    }

    public void markCompleted(Map<String, Object> result) {
        this.result = new LinkedHashMap<>(result);
        this.taskStatus = TaskStatus.PENDING_REVIEW;
        touch();
    }

    public void markFailed(Map<String, Object> result) {
        this.result = new LinkedHashMap<>(result);
        this.taskStatus = TaskStatus.FAILED;
        touch();
    }

    public void recordReview(TaskReview review) {
        this.review = review;
        this.taskStatus = switch (review.reviewStatus()) {
            case APPROVED -> TaskStatus.APPROVED;
            case REJECTED -> TaskStatus.REJECTED;
            case NEEDS_CHANGES -> TaskStatus.NEEDS_CHANGES;
        };
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
