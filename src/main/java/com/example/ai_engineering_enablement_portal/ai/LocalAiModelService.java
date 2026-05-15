package com.example.ai_engineering_enablement_portal.ai;

import com.example.ai_engineering_enablement_portal.task.Exception.TaskDependencyException;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LocalAiModelService implements AiModelService {
    private final RestClient restClient;
    private final String model;

    public LocalAiModelService(
            RestClient.Builder restClientBuilder,
            @Value("${ai.local.base-url}") String baseUrl,
            @Value("${ai.local.model}") String model,
            @Value("${ai.local.timeout-seconds}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.model = model;
    }

    @Override
    public String generate(String prompt) {
        try {
            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", model,
                            "prompt", prompt,
                            "stream", false))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (response == null || response.response() == null || response.response().isBlank()) {
                throw new TaskDependencyException("Local AI model returned an empty response", null);
            }
            return response.response();
        } catch (TaskDependencyException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new TaskDependencyException("Local AI model request failed", exception);
        }
    }

    private record OllamaGenerateResponse(String response) {
    }
}
