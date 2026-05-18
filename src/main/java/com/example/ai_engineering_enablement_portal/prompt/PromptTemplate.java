package com.example.ai_engineering_enablement_portal.prompt;

import com.example.ai_engineering_enablement_portal.agent.AgentFeedback;
import com.example.ai_engineering_enablement_portal.agent.AgentProfile;
import java.util.List;
import java.util.Map;

public record PromptTemplate(String systemInstruction) {
    public String render(
            AgentProfile agentProfile,
            Map<String, Object> payload,
            List<String> retrievalContext,
            List<AgentFeedback> priorFeedback,
            List<AgentFeedback> phaseFeedback,
            String phaseInstruction) {
        return """
                %s

                Active agent profile:
                %s

                Agent role instructions:
                %s

                Phase instruction:
                %s

                Developer task payload:
                %s

                Retrieved context:
                %s

                Prior documented feedback on this task:
                %s

                Feedback to consider for this phase:
                %s

                Return concise, actionable feedback from your agent role. Separate direct findings from questions or assumptions.
                """.formatted(
                systemInstruction,
                agentProfile.displayName(),
                agentProfile.systemPrompt(),
                phaseInstruction,
                payload,
                retrievalContext,
                priorFeedback,
                phaseFeedback);
    }
}
