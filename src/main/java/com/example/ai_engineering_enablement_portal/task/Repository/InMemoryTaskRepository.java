package com.example.ai_engineering_enablement_portal.task.Repository;

import com.example.ai_engineering_enablement_portal.agent.AgentFeedback;
import com.example.ai_engineering_enablement_portal.agent.AgentFeedbackPhase;
import com.example.ai_engineering_enablement_portal.agent.AgentRole;
import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.ReviewStatus;
import com.example.ai_engineering_enablement_portal.task.TaskReview;
import com.example.ai_engineering_enablement_portal.task.TaskStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final ConcurrentHashMap<UUID, AiTask> tasks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final Path taskFilePath;

    public InMemoryTaskRepository(
            @Value("${storage.local.task-file-path:data/tasks.json}") Path taskFilePath) {
        this.objectMapper = new ObjectMapper();
        this.taskFilePath = taskFilePath;
        loadFromDisk();
    }

    @Override
    public AiTask save(AiTask task) {
        tasks.put(task.taskId(), task);
        persistToDisk();
        return task;
    }

    @Override
    public Optional<AiTask> create(AiTask task) {
        AiTask existing = tasks.putIfAbsent(task.taskId(), task);
        if (existing != null) {
            return Optional.empty();
        }
        persistToDisk();
        return Optional.of(task);
    }

    @Override
    public Optional<AiTask> findById(UUID taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public boolean existsById(UUID taskId) {
        return tasks.containsKey(taskId);
    }

    @Override
    public List<AiTask> findAll() {
        return tasks.values().stream()
                .sorted(Comparator.comparing(AiTask::createdAt).reversed())
                .toList();
    }

    private synchronized void loadFromDisk() {
        if (!Files.exists(taskFilePath)) {
            return;
        }
        try {
            List<TaskSnapshot> snapshots = objectMapper.readValue(
                    taskFilePath.toFile(),
                    new TypeReference<>() {
                    });
            snapshots.stream()
                    .map(TaskSnapshot::toTask)
                    .forEach(task -> tasks.put(task.taskId(), task));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load local task store from " + taskFilePath, exception);
        }
    }

    private synchronized void persistToDisk() {
        try {
            Path parent = taskFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<TaskSnapshot> snapshots = tasks.values().stream()
                    .sorted(Comparator.comparing(AiTask::createdAt))
                    .map(TaskSnapshot::from)
                    .toList();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(taskFilePath.toFile(), snapshots);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to persist local task store to " + taskFilePath, exception);
        }
    }

    private record TaskSnapshot(
            UUID taskId,
            Map<String, Object> payload,
            String taskStatus,
            Map<String, Object> result,
            ReviewSnapshot review,
            List<AgentFeedbackSnapshot> agentFeedback,
            String createdAt,
            String updatedAt) {

        static TaskSnapshot from(AiTask task) {
            return new TaskSnapshot(
                    task.taskId(),
                    task.payload(),
                    task.taskStatus().name(),
                    task.result(),
                    ReviewSnapshot.from(task.review()),
                    task.agentFeedback().stream().map(AgentFeedbackSnapshot::from).toList(),
                    task.createdAt().toString(),
                    task.updatedAt().toString());
        }

        AiTask toTask() {
            List<AgentFeedback> restoredFeedback = agentFeedback == null
                    ? List.of()
                    : agentFeedback.stream().map(AgentFeedbackSnapshot::toFeedback).toList();
            return new AiTask(
                    taskId,
                    payload,
                    TaskStatus.valueOf(taskStatus),
                    result,
                    review == null ? null : review.toReview(),
                    restoredFeedback,
                    Instant.parse(createdAt),
                    Instant.parse(updatedAt));
        }
    }

    private record ReviewSnapshot(
            String reviewStatus,
            String reviewComments,
            String reviewedAt) {

        static ReviewSnapshot from(TaskReview review) {
            if (review == null) {
                return null;
            }
            return new ReviewSnapshot(
                    review.reviewStatus().name(),
                    review.reviewComments(),
                    review.reviewedAt().toString());
        }

        TaskReview toReview() {
            return new TaskReview(
                    ReviewStatus.valueOf(reviewStatus),
                    reviewComments,
                    Instant.parse(reviewedAt));
        }
    }

    private record AgentFeedbackSnapshot(
            UUID analysisRunId,
            String agentRole,
            String agentName,
            String phase,
            String content,
            String createdAt) {

        static AgentFeedbackSnapshot from(AgentFeedback feedback) {
            return new AgentFeedbackSnapshot(
                    feedback.analysisRunId(),
                    feedback.agentRole().name(),
                    feedback.agentName(),
                    feedback.phase().name(),
                    feedback.content(),
                    feedback.createdAt().toString());
        }

        AgentFeedback toFeedback() {
            return new AgentFeedback(
                    analysisRunId,
                    AgentRole.valueOf(agentRole),
                    agentName,
                    AgentFeedbackPhase.valueOf(phase),
                    content,
                    Instant.parse(createdAt));
        }
    }
}
