package com.example.ai_engineering_enablement_portal.agent.Controller;

import com.example.ai_engineering_enablement_portal.agent.AgentProfileService;
import com.example.ai_engineering_enablement_portal.agent.Api.AgentProfileResponse;
import com.example.ai_engineering_enablement_portal.agent.Api.TaskTypeRouteResponse;
import com.example.ai_engineering_enablement_portal.agent.Api.UpsertAgentProfileRequest;
import com.example.ai_engineering_enablement_portal.agent.Api.UpsertTaskTypeRouteRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentConfigurationController {
    private final AgentProfileService agentProfileService;

    public AgentConfigurationController(AgentProfileService agentProfileService) {
        this.agentProfileService = agentProfileService;
    }

    @GetMapping("/agent-profiles")
    public List<AgentProfileResponse> listAgentProfiles() {
        return agentProfileService.listProfiles().stream()
                .map(AgentProfileResponse::from)
                .toList();
    }

    @PostMapping("/agent-profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentProfileResponse upsertAgentProfile(@Valid @RequestBody UpsertAgentProfileRequest request) {
        return AgentProfileResponse.from(agentProfileService.saveProfile(request.toProfile()));
    }

    @GetMapping("/task-types")
    public List<TaskTypeRouteResponse> listTaskTypeRoutes() {
        return agentProfileService.listTaskTypeRoutes().stream()
                .map(TaskTypeRouteResponse::from)
                .toList();
    }

    @PostMapping("/task-types")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskTypeRouteResponse upsertTaskTypeRoute(@Valid @RequestBody UpsertTaskTypeRouteRequest request) {
        return TaskTypeRouteResponse.from(agentProfileService.saveTaskTypeRoute(request.toRoute()));
    }
}
