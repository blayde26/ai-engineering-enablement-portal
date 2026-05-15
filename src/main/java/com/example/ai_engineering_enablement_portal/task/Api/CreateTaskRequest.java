package com.example.ai_engineering_enablement_portal.task.Api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record CreateTaskRequest(
        @JsonProperty("task_id") @NotNull UUID taskId,
        @NotEmpty Map<String, Object> data) {
}
