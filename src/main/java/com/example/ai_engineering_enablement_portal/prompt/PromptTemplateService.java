package com.example.ai_engineering_enablement_portal.prompt;

import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {
    public PromptTemplate agentAnalysisTemplate() {
        return new PromptTemplate("""
                You are part of an AI engineering enablement review board.
                Do not invent facts that are not present in the payload, retrieved context, or documented feedback.
                When critiquing another agent, challenge assumptions and gaps without restating their whole answer.
                """);
    }
}
