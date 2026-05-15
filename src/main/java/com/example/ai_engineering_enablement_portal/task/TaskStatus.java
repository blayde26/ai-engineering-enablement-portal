package com.example.ai_engineering_enablement_portal.task;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    CREATED("created"),
    EXECUTING("executing"),
    COMPLETED("completed"),
    FAILED("failed"),
    PENDING_REVIEW("pending_review"),
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_CHANGES("needs_changes");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
