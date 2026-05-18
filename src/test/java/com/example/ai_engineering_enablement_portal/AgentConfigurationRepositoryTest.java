package com.example.ai_engineering_enablement_portal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.ai_engineering_enablement_portal.agent.AgentConfigurationRepository;
import com.example.ai_engineering_enablement_portal.agent.AgentProfile;
import com.example.ai_engineering_enablement_portal.agent.TaskTypeRoute;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentConfigurationRepositoryTest {
    @TempDir
    private Path tempDir;

    @Test
    void persistsCustomProfilesAndRoutesAcrossInstances() {
        Path store = tempDir.resolve("agent-routing.json");
        AgentConfigurationRepository repository = new AgentConfigurationRepository(store);
        repository.saveProfile(new AgentProfile(
                "ux_reviewer",
                "UX Reviewer",
                "Focus on usability, clarity, and workflow friction."));
        repository.saveRoute(new TaskTypeRoute("experience_review", List.of("ux_reviewer", "product_owner")));

        AgentConfigurationRepository reloaded = new AgentConfigurationRepository(store);

        assertEquals("UX Reviewer", reloaded.findProfile("ux_reviewer").orElseThrow().displayName());
        assertEquals(List.of("ux_reviewer", "product_owner"), reloaded.findRoute("experience_review").orElseThrow().agentIds());
    }
}
