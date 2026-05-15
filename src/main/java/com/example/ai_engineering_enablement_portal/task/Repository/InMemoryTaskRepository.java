package com.example.ai_engineering_enablement_portal.task.Repository;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final ConcurrentHashMap<UUID, AiTask> tasks = new ConcurrentHashMap<>();

    @Override
    public AiTask save(AiTask task) {
        tasks.put(task.taskId(), task);
        return task;
    }

    @Override
    public Optional<AiTask> create(AiTask task) {
        AiTask existing = tasks.putIfAbsent(task.taskId(), task);
        return existing == null ? Optional.of(task) : Optional.empty();
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
}
