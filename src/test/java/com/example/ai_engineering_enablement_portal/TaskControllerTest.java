package com.example.ai_engineering_enablement_portal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "ai.local.base-url=http://127.0.0.1:1",
        "ai.local.model=test-model",
        "ai.local.timeout-seconds=1"
})
class TaskControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void createRetrieveListAndAuditTask() {
        UUID taskId = UUID.randomUUID();

        webTestClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "task_id": "%s",
                          "data": {
                            "task_type": "code_review",
                            "prompt": "Review this controller for validation issues"
                          }
                        }
                        """.formatted(taskId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.task_id").isEqualTo(taskId.toString());

        webTestClient.get()
                .uri("/tasks/{task_id}", taskId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.task_id").isEqualTo(taskId.toString())
                .jsonPath("$.data.task_status").isEqualTo("created");

        webTestClient.get()
                .uri("/tasks?page_number=1&page_size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.total_count").value(Integer.class, count -> assertTrue(count >= 1));

        webTestClient.post()
                .uri("/tasks/{task_id}/audit", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "review_status": "approved",
                          "review_comments": "Looks acceptable for the first implementation."
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Task audited successfully");
    }

    @Test
    void createTaskRejectsDuplicateTaskId() {
        UUID taskId = UUID.randomUUID();
        String requestBody = """
                {
                  "task_id": "%s",
                  "data": {
                    "task_type": "test_generation",
                    "prompt": "Generate unit tests"
                  }
                }
                """.formatted(taskId);

        webTestClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("CONFLICT");
    }

    @Test
    void executeTaskReturnsDependencyFailureWhenLocalModelUnavailable() {
        UUID taskId = UUID.randomUUID();

        webTestClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "task_id": "%s",
                          "data": {
                            "task_type": "architecture_critique",
                            "prompt": "Review this architecture"
                          }
                        }
                        """.formatted(taskId))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/tasks/{task_id}/execution", taskId)
                .exchange()
                .expectStatus().isEqualTo(424)
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("DEPENDENCY_FAILURE");
    }

    @Test
    void retrieveTaskReturnsNotFound() {
        webTestClient.get()
                .uri("/tasks/{task_id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("TASK_NOT_FOUND");
    }
}
