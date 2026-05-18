package com.example.ai_engineering_enablement_portal.agent;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentRole {
    TEST_ENGINEER("test_engineer"),
    PRINCIPAL_ENGINEER("principal_engineer"),
    SECURITY_ENGINEER("security_engineer"),
    PRODUCT_OWNER("product_owner");

    private final String value;

    AgentRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
