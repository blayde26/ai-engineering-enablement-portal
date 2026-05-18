package com.example.ai_engineering_enablement_portal.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record AgentFeedback(
        @JsonProperty("analysis_run_id") UUID analysisRunId,
        @JsonProperty("agent_id") String agentId,
        @JsonProperty("agent_name") String agentName,
        AgentFeedbackPhase phase,
        String content,
        @JsonProperty("created_at") Instant createdAt) {
}
