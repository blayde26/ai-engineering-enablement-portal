package com.example.ai_engineering_enablement_portal.agent.Api;

import com.example.ai_engineering_enablement_portal.agent.AgentProfile;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentProfileResponse(
        @JsonProperty("agent_id") String agentId,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("system_prompt") String systemPrompt) {

    public static AgentProfileResponse from(AgentProfile profile) {
        return new AgentProfileResponse(
                profile.agentId(),
                profile.displayName(),
                profile.systemPrompt());
    }
}
