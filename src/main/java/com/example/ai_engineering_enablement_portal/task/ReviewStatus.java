package com.example.ai_engineering_enablement_portal.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewStatus {
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_CHANGES("needs_changes");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ReviewStatus fromValue(String value) {
        for (ReviewStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported review_status: " + value);
    }

    @JsonValue
    public String value() {
        return value;
    }
}
