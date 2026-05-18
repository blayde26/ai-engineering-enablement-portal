package com.example.ai_engineering_enablement_portal.agent.Api;

import com.example.ai_engineering_enablement_portal.agent.TaskTypeRoute;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TaskTypeRouteResponse(
        @JsonProperty("task_type") String taskType,
        @JsonProperty("agent_ids") List<String> agentIds) {

    public static TaskTypeRouteResponse from(TaskTypeRoute route) {
        return new TaskTypeRouteResponse(route.taskType(), route.agentIds());
    }
}
