package com.example.ai_engineering_enablement_portal.agent;

import java.util.List;

public record TaskTypeRoute(
        String taskType,
        List<String> agentIds) {
}
