package com.example.ai_engineering_enablement_portal.task.Service;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskListingService {
    private final TaskRepository taskRepository;

    public TaskListingService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page list(int pageNumber, int pageSize) {
        List<AiTask> allTasks = taskRepository.findAll();
        int fromIndex = Math.min((pageNumber - 1) * pageSize, allTasks.size());
        int toIndex = Math.min(fromIndex + pageSize, allTasks.size());
        return new Page(allTasks.subList(fromIndex, toIndex), allTasks.size(), pageNumber, pageSize);
    }

    public record Page(List<AiTask> tasks, int totalCount, int pageNumber, int pageSize) {
    }
}
