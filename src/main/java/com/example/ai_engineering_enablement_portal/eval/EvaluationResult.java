package com.example.ai_engineering_enablement_portal.eval;

import java.util.List;

public record EvaluationResult(
        double score,
        String label,
        List<String> findings) {
}
