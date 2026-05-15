package com.example.ai_engineering_enablement_portal.task.Api;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record TaskSummaryResponse(
        @JsonProperty("task_id") UUID taskId,
        @JsonProperty("task_status") TaskStatus taskStatus,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt) {

    public static TaskSummaryResponse from(AiTask task) {
        return new TaskSummaryResponse(
                task.taskId(),
                task.taskStatus(),
                task.createdAt(),
                task.updatedAt());
    }
}
