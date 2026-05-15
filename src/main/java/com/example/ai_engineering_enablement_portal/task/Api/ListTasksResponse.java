package com.example.ai_engineering_enablement_portal.task.Api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ListTasksResponse(
        List<TaskSummaryResponse> tasks,
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("page_number") int pageNumber,
        @JsonProperty("page_size") int pageSize) {
}
