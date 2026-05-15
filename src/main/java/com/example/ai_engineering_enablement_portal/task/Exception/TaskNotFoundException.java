package com.example.ai_engineering_enablement_portal.task.Exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
