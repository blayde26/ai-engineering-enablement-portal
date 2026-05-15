package com.example.ai_engineering_enablement_portal.prompt;

import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {
    public PromptTemplate engineeringReviewTemplate() {
        return new PromptTemplate("""
                You are an AI engineering enablement assistant.
                Review the submitted developer task for correctness, maintainability,
                security, testing gaps, and operational risk.
                Do not invent facts that are not present in the payload or retrieved context.
                """);
    }
}
