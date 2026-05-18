package com.example.ai_engineering_enablement_portal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.ai_engineering_enablement_portal.agent.AgentProfileService;
import com.example.ai_engineering_enablement_portal.agent.AgentRole;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentProfileServiceTest {
    private final AgentProfileService agentProfileService = new AgentProfileService();

    @Test
    void routesUserStoryToProductAndPrincipalAgents() {
        List<AgentRole> roles = agentProfileService.profilesFor(Map.of("task_type", "user_story"))
                .stream()
                .map(profile -> profile.role())
                .toList();

        assertEquals(List.of(AgentRole.PRODUCT_OWNER, AgentRole.PRINCIPAL_ENGINEER), roles);
    }

    @Test
    void routesWorkPlanToPrincipalTestAndSecurityAgents() {
        List<AgentRole> roles = agentProfileService.profilesFor(Map.of("task_type", "work_plan"))
                .stream()
                .map(profile -> profile.role())
                .toList();

        assertEquals(List.of(AgentRole.PRINCIPAL_ENGINEER, AgentRole.TEST_ENGINEER, AgentRole.SECURITY_ENGINEER), roles);
    }

    @Test
    void defaultsUnknownTaskTypeToPrincipalEngineer() {
        List<AgentRole> roles = agentProfileService.profilesFor(Map.of("task_type", "unknown"))
                .stream()
                .map(profile -> profile.role())
                .toList();

        assertEquals(List.of(AgentRole.PRINCIPAL_ENGINEER), roles);
    }
}
