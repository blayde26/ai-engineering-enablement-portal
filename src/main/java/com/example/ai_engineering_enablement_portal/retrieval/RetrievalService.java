package com.example.ai_engineering_enablement_portal.retrieval;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import java.util.List;
import java.util.Map;

public interface RetrievalService {
    List<String> retrieveContext(AiTask task);

    void indexResult(AiTask task, Map<String, Object> result);
}
