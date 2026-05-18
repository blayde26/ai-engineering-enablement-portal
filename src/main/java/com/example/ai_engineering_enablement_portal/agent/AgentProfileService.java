package com.example.ai_engineering_enablement_portal.agent;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AgentProfileService {
    private static final AgentProfile TEST_ENGINEER = new AgentProfile(
            AgentRole.TEST_ENGINEER,
            "Test Engineer",
            "You are a senior test engineer. Focus on testability, missing test cases, edge cases, regression risk, automation strategy, and measurable acceptance criteria.");

    private static final AgentProfile PRINCIPAL_ENGINEER = new AgentProfile(
            AgentRole.PRINCIPAL_ENGINEER,
            "Principal Engineer",
            "You are a principal engineer. Focus on correctness, maintainability, system design, scalability, operational risk, and implementation tradeoffs.");

    private static final AgentProfile SECURITY_ENGINEER = new AgentProfile(
            AgentRole.SECURITY_ENGINEER,
            "Security Engineer",
            "You are a security engineer. Focus on threat modeling, abuse cases, data exposure, authorization, dependency risk, and secure-by-default mitigations.");

    private static final AgentProfile PRODUCT_OWNER = new AgentProfile(
            AgentRole.PRODUCT_OWNER,
            "Product Owner",
            "You are a product owner. Focus on user value, scope clarity, acceptance criteria, prioritization, stakeholder impact, and delivery sequencing.");

    public List<AgentProfile> profilesFor(Map<String, Object> payload) {
        String taskType = normalizeTaskType(payload.get("task_type"));
        return switch (taskType) {
            case "user_story", "documentation_question", "incident_summary" -> List.of(PRODUCT_OWNER, PRINCIPAL_ENGINEER);
            case "work_plan" -> List.of(PRINCIPAL_ENGINEER, TEST_ENGINEER, SECURITY_ENGINEER);
            case "test_generation" -> List.of(TEST_ENGINEER, PRINCIPAL_ENGINEER);
            case "security_review" -> List.of(SECURITY_ENGINEER, PRINCIPAL_ENGINEER, TEST_ENGINEER);
            case "architecture_critique" -> List.of(PRINCIPAL_ENGINEER, SECURITY_ENGINEER);
            case "code_review" -> List.of(PRINCIPAL_ENGINEER, TEST_ENGINEER, SECURITY_ENGINEER);
            default -> List.of(PRINCIPAL_ENGINEER);
        };
    }

    private String normalizeTaskType(Object taskType) {
        return Objects.toString(taskType, "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
