package com.example.ai_engineering_enablement_portal.agent.Api;

import com.example.ai_engineering_enablement_portal.agent.AgentProfile;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertAgentProfileRequest(
        @JsonProperty("agent_id")
        @NotBlank
        @Size(max = 64)
        String agentId,
        @JsonProperty("display_name")
        @NotBlank
        @Size(max = 120)
        String displayName,
        @JsonProperty("system_prompt")
        @NotBlank
        @Size(max = 5000)
        String systemPrompt) {

    public AgentProfile toProfile() {
        return new AgentProfile(agentId, displayName, systemPrompt);
    }
}
