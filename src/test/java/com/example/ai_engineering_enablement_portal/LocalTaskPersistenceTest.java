package com.example.ai_engineering_enablement_portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.ai_engineering_enablement_portal.agent.AgentFeedback;
import com.example.ai_engineering_enablement_portal.agent.AgentFeedbackPhase;
import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Repository.InMemoryTaskRepository;
import com.example.ai_engineering_enablement_portal.task.ReviewStatus;
import com.example.ai_engineering_enablement_portal.task.TaskReview;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalTaskPersistenceTest {
    @TempDir
    private Path tempDir;

    @Test
    void persistsTasksAcrossRepositoryInstances() {
        Path store = tempDir.resolve("tasks.json");
        UUID taskId = UUID.randomUUID();
        UUID analysisRunId = UUID.randomUUID();

        InMemoryTaskRepository repository = new InMemoryTaskRepository(store);
        AiTask task = new AiTask(taskId, Map.of(
                "task_type", "work_plan",
                "prompt", "Plan a local persistence feature"));
        repository.create(task);
        task.recordAgentFeedback(new AgentFeedback(
                analysisRunId,
                "principal_engineer",
                "Principal Engineer",
                AgentFeedbackPhase.INITIAL_ANALYSIS,
                "Use a simple local file-backed store.",
                Instant.now()));
        task.recordReview(new TaskReview(
                ReviewStatus.APPROVED,
                "Looks good for local development.",
                Instant.now()));
        repository.save(task);

        InMemoryTaskRepository reloadedRepository = new InMemoryTaskRepository(store);
        AiTask reloaded = reloadedRepository.findById(taskId).orElseThrow();

        assertEquals("work_plan", reloaded.payload().get("task_type"));
        assertEquals(ReviewStatus.APPROVED, reloaded.review().reviewStatus());
        assertEquals(1, reloaded.agentFeedback().size());
        assertEquals("principal_engineer", reloaded.agentFeedback().getFirst().agentId());
        assertTrue(store.toFile().length() > 0);
    }
}
