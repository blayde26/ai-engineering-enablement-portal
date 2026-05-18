package com.example.ai_engineering_enablement_portal.agent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AgentProfileService {
    private final AgentConfigurationRepository agentConfigurationRepository;

    public AgentProfileService(AgentConfigurationRepository agentConfigurationRepository) {
        this.agentConfigurationRepository = agentConfigurationRepository;
    }

    public List<AgentProfile> listProfiles() {
        return agentConfigurationRepository.findAllProfiles();
    }

    public AgentProfile saveProfile(AgentProfile profile) {
        validateProfile(profile);
        return agentConfigurationRepository.saveProfile(profile);
    }

    public List<TaskTypeRoute> listTaskTypeRoutes() {
        return agentConfigurationRepository.findAllRoutes();
    }

    public TaskTypeRoute saveTaskTypeRoute(TaskTypeRoute route) {
        validateRoute(route);
        return agentConfigurationRepository.saveRoute(route);
    }

    public List<AgentProfile> profilesFor(Map<String, Object> payload) {
        String taskType = normalizeTaskType(payload.get("task_type"));
        TaskTypeRoute route = agentConfigurationRepository.findRoute(taskType)
                .orElseGet(() -> new TaskTypeRoute(taskType, List.of(AgentConfigurationRepository.PRINCIPAL_ENGINEER_ID)));
        List<AgentProfile> routedProfiles = route.agentIds().stream()
                .map(agentConfigurationRepository::findProfile)
                .flatMap(Optional::stream)
                .toList();
        if (routedProfiles.isEmpty()) {
            return agentConfigurationRepository.findProfile(AgentConfigurationRepository.PRINCIPAL_ENGINEER_ID)
                    .map(List::of)
                    .orElse(List.of());
        }
        return routedProfiles;
    }

    private void validateProfile(AgentProfile profile) {
        if (isBlank(profile.agentId()) || isBlank(profile.displayName()) || isBlank(profile.systemPrompt())) {
            throw new IllegalArgumentException("Agent profile requires agent_id, display_name, and system_prompt.");
        }
        String agentId = normalizeTaskType(profile.agentId());
        if (!agentId.matches("[a-z0-9_]{3,64}")) {
            throw new IllegalArgumentException("agent_id must be 3-64 lowercase letters, numbers, or underscores.");
        }
        if (profile.systemPrompt().length() > 5000) {
            throw new IllegalArgumentException("system_prompt must be 5000 characters or fewer.");
        }
    }

    private void validateRoute(TaskTypeRoute route) {
        if (isBlank(route.taskType()) || route.agentIds() == null || route.agentIds().isEmpty()) {
            throw new IllegalArgumentException("Task type route requires task_type and at least one agent_id.");
        }
        String taskType = normalizeTaskType(route.taskType());
        if (!taskType.matches("[a-z0-9_]{3,64}")) {
            throw new IllegalArgumentException("task_type must be 3-64 lowercase letters, numbers, or underscores.");
        }
        Set<String> missingAgents = new LinkedHashSet<>();
        route.agentIds().stream()
                .map(this::normalizeTaskType)
                .forEach(agentId -> {
                    if (agentConfigurationRepository.findProfile(agentId).isEmpty()) {
                        missingAgents.add(agentId);
                    }
                });
        if (!missingAgents.isEmpty()) {
            throw new IllegalArgumentException("Unknown agent_id values: " + missingAgents);
        }
    }

    private String normalizeTaskType(Object taskType) {
        return Objects.toString(taskType, "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
