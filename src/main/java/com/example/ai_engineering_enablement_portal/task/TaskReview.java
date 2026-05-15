package com.example.ai_engineering_enablement_portal.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record TaskReview(
        @JsonProperty("review_status") ReviewStatus reviewStatus,
        @JsonProperty("review_comments") String reviewComments,
        @JsonProperty("reviewed_at") Instant reviewedAt) {
}
