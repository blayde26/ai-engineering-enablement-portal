package com.example.ai_engineering_enablement_portal.agent.Api;

import com.example.ai_engineering_enablement_portal.agent.TaskTypeRoute;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpsertTaskTypeRouteRequest(
        @JsonProperty("task_type")
        @NotBlank
        @Size(max = 64)
        String taskType,
        @JsonProperty("agent_ids")
        @NotEmpty
        @Size(max = 20)
        List<@NotBlank @Size(max = 64) String> agentIds) {

    public TaskTypeRoute toRoute() {
        return new TaskTypeRoute(taskType, agentIds);
    }
}
