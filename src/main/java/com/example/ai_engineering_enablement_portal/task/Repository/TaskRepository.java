package com.example.ai_engineering_enablement_portal.task.Repository;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
    Optional<AiTask> create(AiTask task);

    AiTask save(AiTask task);

    Optional<AiTask> findById(UUID taskId);

    boolean existsById(UUID taskId);

    List<AiTask> findAll();
}
