package com.example.ai_engineering_enablement_portal.agent;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentFeedbackPhase {
    INITIAL_ANALYSIS("initial_analysis"),
    CRITIQUE("critique"),
    RESPONSE("response");

    private final String value;

    AgentFeedbackPhase(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
