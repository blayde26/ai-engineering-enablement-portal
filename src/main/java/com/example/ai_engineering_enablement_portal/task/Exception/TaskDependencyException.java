package com.example.ai_engineering_enablement_portal.task.Exception;

public class TaskDependencyException extends RuntimeException {
    public TaskDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
