package com.example.ai_engineering_enablement_portal.task.Api;

import com.example.ai_engineering_enablement_portal.task.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuditTaskRequest(
        @JsonProperty("review_status") @NotNull ReviewStatus reviewStatus,
        @JsonProperty("review_comments") @NotBlank @Size(max = 5000) String reviewComments) {
}
