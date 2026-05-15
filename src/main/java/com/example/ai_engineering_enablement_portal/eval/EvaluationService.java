package com.example.ai_engineering_enablement_portal.eval;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {
    public EvaluationResult evaluate(String modelOutput, List<String> retrievalContext) {
        List<String> findings = new ArrayList<>();
        double score = 1.0;

        if (modelOutput == null || modelOutput.isBlank()) {
            findings.add("Model output is empty.");
            score -= 0.7;
        }
        if (modelOutput != null && modelOutput.length() < 80) {
            findings.add("Model output is short; reviewer should verify completeness.");
            score -= 0.15;
        }
        if (!retrievalContext.isEmpty() && modelOutput != null
                && retrievalContext.stream().noneMatch(context -> modelOutput.toLowerCase().contains(firstToken(context)))) {
            findings.add("Output does not appear to reference retrieved context.");
            score -= 0.15;
        }

        double normalizedScore = Math.max(0.0, Math.min(1.0, score));
        String label = normalizedScore >= 0.75 ? "pass" : normalizedScore >= 0.5 ? "review" : "fail";
        return new EvaluationResult(normalizedScore, label, findings);
    }

    private String firstToken(String context) {
        String[] tokens = context.toLowerCase().split("\\W+");
        return tokens.length == 0 ? "" : tokens[0];
    }
}
