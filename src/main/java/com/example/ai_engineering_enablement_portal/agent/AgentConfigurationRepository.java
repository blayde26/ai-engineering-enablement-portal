package com.example.ai_engineering_enablement_portal.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AgentConfigurationRepository {
    public static final String TEST_ENGINEER_ID = "test_engineer";
    public static final String PRINCIPAL_ENGINEER_ID = "principal_engineer";
    public static final String SECURITY_ENGINEER_ID = "security_engineer";
    public static final String PRODUCT_OWNER_ID = "product_owner";

    private final Map<String, AgentProfile> profiles = new LinkedHashMap<>();
    private final Map<String, TaskTypeRoute> routes = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path configurationFilePath;

    public AgentConfigurationRepository(
            @Value("${storage.local.agent-config-file-path:data/agent-routing.json}") Path configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
        seedDefaults();
        loadFromDisk();
    }

    public synchronized List<AgentProfile> findAllProfiles() {
        return List.copyOf(profiles.values());
    }

    public synchronized Optional<AgentProfile> findProfile(String agentId) {
        return Optional.ofNullable(profiles.get(normalize(agentId)));
    }

    public synchronized AgentProfile saveProfile(AgentProfile profile) {
        AgentProfile normalized = new AgentProfile(
                normalize(profile.agentId()),
                profile.displayName().trim(),
                profile.systemPrompt().trim());
        profiles.put(normalized.agentId(), normalized);
        persistToDisk();
        return normalized;
    }

    public synchronized List<TaskTypeRoute> findAllRoutes() {
        return List.copyOf(routes.values());
    }

    public synchronized Optional<TaskTypeRoute> findRoute(String taskType) {
        return Optional.ofNullable(routes.get(normalize(taskType)));
    }

    public synchronized TaskTypeRoute saveRoute(TaskTypeRoute route) {
        TaskTypeRoute normalized = new TaskTypeRoute(
                normalize(route.taskType()),
                route.agentIds().stream().map(this::normalize).distinct().toList());
        routes.put(normalized.taskType(), normalized);
        persistToDisk();
        return normalized;
    }

    private void seedDefaults() {
        profiles.put(TEST_ENGINEER_ID, new AgentProfile(
                TEST_ENGINEER_ID,
                "Test Engineer",
                "You are a senior test engineer. Focus on testability, missing test cases, edge cases, regression risk, automation strategy, and measurable acceptance criteria."));
        profiles.put(PRINCIPAL_ENGINEER_ID, new AgentProfile(
                PRINCIPAL_ENGINEER_ID,
                "Principal Engineer",
                "You are a principal engineer. Focus on correctness, maintainability, system design, scalability, operational risk, and implementation tradeoffs."));
        profiles.put(SECURITY_ENGINEER_ID, new AgentProfile(
                SECURITY_ENGINEER_ID,
                "Security Engineer",
                "You are a security engineer. Focus on threat modeling, abuse cases, data exposure, authorization, dependency risk, and secure-by-default mitigations."));
        profiles.put(PRODUCT_OWNER_ID, new AgentProfile(
                PRODUCT_OWNER_ID,
                "Product Owner",
                "You are a product owner. Focus on user value, scope clarity, acceptance criteria, prioritization, stakeholder impact, and delivery sequencing."));

        putDefaultRoute("user_story", PRODUCT_OWNER_ID, PRINCIPAL_ENGINEER_ID);
        putDefaultRoute("documentation_question", PRODUCT_OWNER_ID, PRINCIPAL_ENGINEER_ID);
        putDefaultRoute("incident_summary", PRODUCT_OWNER_ID, PRINCIPAL_ENGINEER_ID);
        putDefaultRoute("work_plan", PRINCIPAL_ENGINEER_ID, TEST_ENGINEER_ID, SECURITY_ENGINEER_ID);
        putDefaultRoute("test_generation", TEST_ENGINEER_ID, PRINCIPAL_ENGINEER_ID);
        putDefaultRoute("security_review", SECURITY_ENGINEER_ID, PRINCIPAL_ENGINEER_ID, TEST_ENGINEER_ID);
        putDefaultRoute("architecture_critique", PRINCIPAL_ENGINEER_ID, SECURITY_ENGINEER_ID);
        putDefaultRoute("code_review", PRINCIPAL_ENGINEER_ID, TEST_ENGINEER_ID, SECURITY_ENGINEER_ID);
    }

    private void putDefaultRoute(String taskType, String... agentIds) {
        routes.put(taskType, new TaskTypeRoute(taskType, List.of(agentIds)));
    }

    private void loadFromDisk() {
        if (!Files.exists(configurationFilePath)) {
            return;
        }
        try {
            AgentConfigurationSnapshot snapshot = objectMapper.readValue(
                    configurationFilePath.toFile(),
                    new TypeReference<>() {
                    });
            if (snapshot.profiles() != null) {
                snapshot.profiles().forEach(profile -> profiles.put(normalize(profile.agentId()), profile));
            }
            if (snapshot.taskTypeRoutes() != null) {
                snapshot.taskTypeRoutes().forEach(route -> routes.put(normalize(route.taskType()), route));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load local agent configuration from " + configurationFilePath, exception);
        }
    }

    private void persistToDisk() {
        try {
            Path parent = configurationFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            AgentConfigurationSnapshot snapshot = new AgentConfigurationSnapshot(
                    List.copyOf(profiles.values()),
                    List.copyOf(routes.values()));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configurationFilePath.toFile(), snapshot);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to persist local agent configuration to " + configurationFilePath, exception);
        }
    }

    private String normalize(String value) {
        return Objects.toString(value, "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private record AgentConfigurationSnapshot(
            List<AgentProfile> profiles,
            List<TaskTypeRoute> taskTypeRoutes) {
    }
}
