package com.example.ai_engineering_enablement_portal.task.Api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record OperationResponse(
        String status,
        String message,
        @JsonProperty("task_id") UUID taskId) {

    public static OperationResponse success(String message, UUID taskId) {
        return new OperationResponse("success", message, taskId);
    }
}
