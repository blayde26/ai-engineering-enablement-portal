package com.example.ai_engineering_enablement_portal.task.Api;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.TaskReview;
import com.example.ai_engineering_enablement_portal.task.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TaskResponse(
        @JsonProperty("task_id") UUID taskId,
        @JsonProperty("task_status") TaskStatus taskStatus,
        Map<String, Object> payload,
        Map<String, Object> result,
        TaskReview review,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt) {

    public static TaskResponse from(AiTask task) {
        return new TaskResponse(
                task.taskId(),
                task.taskStatus(),
                task.payload(),
                task.result(),
                task.review(),
                task.createdAt(),
                task.updatedAt());
    }
}
