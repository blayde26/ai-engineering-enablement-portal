package com.example.ai_engineering_enablement_portal.agent;

public record AgentProfile(
        AgentRole role,
        String displayName,
        String systemPrompt) {
}
