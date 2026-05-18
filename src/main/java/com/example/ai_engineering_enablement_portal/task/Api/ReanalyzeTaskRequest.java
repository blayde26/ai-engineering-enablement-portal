package com.example.ai_engineering_enablement_portal.task.Api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReanalyzeTaskRequest(
        @JsonProperty("agent_collaboration") Boolean agentCollaboration) {

    public boolean agentCollaborationEnabled() {
        return Boolean.TRUE.equals(agentCollaboration);
    }
}
