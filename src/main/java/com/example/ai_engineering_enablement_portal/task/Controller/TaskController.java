package com.example.ai_engineering_enablement_portal.task.Controller;

import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Api.AuditTaskRequest;
import com.example.ai_engineering_enablement_portal.task.Api.CreateTaskRequest;
import com.example.ai_engineering_enablement_portal.task.Api.ListTasksResponse;
import com.example.ai_engineering_enablement_portal.task.Api.OperationResponse;
import com.example.ai_engineering_enablement_portal.task.Api.RetrieveTaskResponse;
import com.example.ai_engineering_enablement_portal.task.Api.TaskResponse;
import com.example.ai_engineering_enablement_portal.task.Api.TaskSummaryResponse;
import com.example.ai_engineering_enablement_portal.task.Service.TaskAuditService;
import com.example.ai_engineering_enablement_portal.task.Service.TaskCreationService;
import com.example.ai_engineering_enablement_portal.task.Service.TaskExecutionService;
import com.example.ai_engineering_enablement_portal.task.Service.TaskListingService;
import com.example.ai_engineering_enablement_portal.task.Service.TaskRetrievalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class TaskController {
    private final TaskCreationService taskCreationService;
    private final TaskRetrievalService taskRetrievalService;
    private final TaskListingService taskListingService;
    private final TaskExecutionService taskExecutionService;
    private final TaskAuditService taskAuditService;

    public TaskController(
            TaskCreationService taskCreationService,
            TaskRetrievalService taskRetrievalService,
            TaskListingService taskListingService,
            TaskExecutionService taskExecutionService,
            TaskAuditService taskAuditService) {
        this.taskCreationService = taskCreationService;
        this.taskRetrievalService = taskRetrievalService;
        this.taskListingService = taskListingService;
        this.taskExecutionService = taskExecutionService;
        this.taskAuditService = taskAuditService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<OperationResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        AiTask task = taskCreationService.create(request.taskId(), request.data());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OperationResponse.success("Task created successfully", task.taskId()));
    }

    @GetMapping("/tasks")
    public ListTasksResponse listTasks(
            @RequestParam(name = "page_number", defaultValue = "1") @Min(1) int pageNumber,
            @RequestParam(name = "page_size", defaultValue = "25") @Min(1) @Max(100) int pageSize) {
        TaskListingService.Page page = taskListingService.list(pageNumber, pageSize);
        return new ListTasksResponse(
                page.tasks().stream().map(TaskSummaryResponse::from).toList(),
                page.totalCount(),
                page.pageNumber(),
                page.pageSize());
    }

    @GetMapping("/tasks/{task_id}")
    public RetrieveTaskResponse retrieveTask(@PathVariable("task_id") UUID taskId) {
        return RetrieveTaskResponse.success(TaskResponse.from(taskRetrievalService.retrieve(taskId)));
    }

    @PostMapping("/tasks/{task_id}/execution")
    public OperationResponse executeTask(@PathVariable("task_id") UUID taskId) {
        AiTask task = taskExecutionService.execute(taskId);
        return OperationResponse.success("Task executed successfully", task.taskId());
    }

    @PostMapping("/tasks/{task_id}/audit")
    public OperationResponse auditTask(
            @PathVariable("task_id") UUID taskId,
            @Valid @RequestBody AuditTaskRequest request) {
        AiTask task = taskAuditService.audit(taskId, request.reviewStatus(), request.reviewComments());
        return OperationResponse.success("Task audited successfully", task.taskId());
    }
}
