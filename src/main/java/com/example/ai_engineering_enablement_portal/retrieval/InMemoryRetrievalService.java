package com.example.ai_engineering_enablement_portal.retrieval;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class InMemoryRetrievalService implements RetrievalService {
    private final CopyOnWriteArrayList<IndexedDocument> documents = new CopyOnWriteArrayList<>();

    @Override
    public List<String> retrieveContext(AiTask task) {
        Set<String> queryTerms = tokenize(task.payload().toString());
        return documents.stream()
                .filter(document -> document.hasAnyTerm(queryTerms))
                .limit(3)
                .map(IndexedDocument::content)
                .toList();
    }

    @Override
    public void indexResult(AiTask task, Map<String, Object> result) {
        String content = "Task %s payload=%s result=%s".formatted(task.taskId(), task.payload(), result);
        documents.add(new IndexedDocument(content, tokenize(content)));
    }

    private Set<String> tokenize(String value) {
        Set<String> terms = new LinkedHashSet<>();
        Arrays.stream(value.toLowerCase().split("\\W+"))
                .filter(token -> token.length() > 3)
                .forEach(terms::add);
        return terms;
    }

    private record IndexedDocument(String content, Set<String> terms) {
        boolean hasAnyTerm(Set<String> queryTerms) {
            return queryTerms.stream().anyMatch(terms::contains);
        }
    }
}
