package com.example.ai_engineering_enablement_portal.task.Service;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Exception.TaskNotFoundException;
import com.example.ai_engineering_enablement_portal.task.Repository.TaskRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TaskRetrievalService {
    private final TaskRepository taskRepository;

    public TaskRetrievalService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public AiTask retrieve(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }
}
