package com.example.ai_engineering_enablement_portal.prompt;

import java.util.List;
import java.util.Map;

public record PromptTemplate(String systemInstruction) {
    public String render(Map<String, Object> payload, List<String> retrievalContext) {
        return """
                %s

                Developer task payload:
                %s

                Retrieved context:
                %s

                Return a concise engineering review with:
                - assessment
                - risks
                - recommended next actions
                """.formatted(systemInstruction, payload, retrievalContext);
    }
}
