package com.example.ai_engineering_enablement_portal.task.Api;

public record RetrieveTaskResponse(
        String status,
        TaskResponse data,
        String message) {

    public static RetrieveTaskResponse success(TaskResponse data) {
        return new RetrieveTaskResponse("success", data, "Task retrieved successfully");
    }
}
