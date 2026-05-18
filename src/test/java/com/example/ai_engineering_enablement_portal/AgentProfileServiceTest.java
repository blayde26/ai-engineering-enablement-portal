package com.example.ai_engineering_enablement_portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.ai_engineering_enablement_portal.agent.AgentConfigurationRepository;
import com.example.ai_engineering_enablement_portal.agent.AgentProfile;
import com.example.ai_engineering_enablement_portal.agent.AgentProfileService;
import com.example.ai_engineering_enablement_portal.agent.TaskTypeRoute;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentProfileServiceTest {
    @TempDir
    private Path tempDir;
    private AgentProfileService agentProfileService;

    @BeforeEach
    void setUp() {
        agentProfileService = new AgentProfileService(new AgentConfigurationRepository(tempDir.resolve("agents.json")));
    }

    @Test
    void routesUserStoryToProductAndPrincipalAgents() {
        List<String> agentIds = agentProfileService.profilesFor(Map.of("task_type", "user_story"))
                .stream()
                .map(AgentProfile::agentId)
                .toList();

        assertEquals(List.of("product_owner", "principal_engineer"), agentIds);
    }

    @Test
    void routesWorkPlanToPrincipalTestAndSecurityAgents() {
        List<String> agentIds = agentProfileService.profilesFor(Map.of("task_type", "work_plan"))
                .stream()
                .map(AgentProfile::agentId)
                .toList();

        assertEquals(List.of("principal_engineer", "test_engineer", "security_engineer"), agentIds);
    }

    @Test
    void defaultsUnknownTaskTypeToPrincipalEngineer() {
        List<String> agentIds = agentProfileService.profilesFor(Map.of("task_type", "unknown"))
                .stream()
                .map(AgentProfile::agentId)
                .toList();

        assertEquals(List.of("principal_engineer"), agentIds);
    }

    @Test
    void routesCustomTaskTypeToCustomAgent() {
        agentProfileService.saveProfile(new AgentProfile(
                "release_manager",
                "Release Manager",
                "Focus on release sequencing, rollback, and stakeholder readiness."));
        agentProfileService.saveTaskTypeRoute(new TaskTypeRoute("release_plan", List.of("release_manager", "test_engineer")));

        List<String> agentIds = agentProfileService.profilesFor(Map.of("task_type", "release_plan"))
                .stream()
                .map(AgentProfile::agentId)
                .toList();

        assertEquals(List.of("release_manager", "test_engineer"), agentIds);
    }

    @Test
    void rejectsRouteWithUnknownAgent() {
        assertThrows(IllegalArgumentException.class,
                () -> agentProfileService.saveTaskTypeRoute(new TaskTypeRoute("bad_route", List.of("missing_agent"))));
    }
}
