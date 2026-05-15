package com.example.ai_engineering_enablement_portal.task.Service;

import com.example.ai_engineering_enablement_portal.ai.AiModelService;
import com.example.ai_engineering_enablement_portal.audit.AuditLoggingService;
import com.example.ai_engineering_enablement_portal.eval.EvaluationResult;
import com.example.ai_engineering_enablement_portal.eval.EvaluationService;
import com.example.ai_engineering_enablement_portal.prompt.PromptTemplateService;
import com.example.ai_engineering_enablement_portal.retrieval.RetrievalService;
import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Exception.TaskDependencyException;
import com.example.ai_engineering_enablement_portal.task.Repository.TaskRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TaskExecutionService {
    private final TaskRetrievalService taskRetrievalService;
    private final TaskRepository taskRepository;
    private final RetrievalService retrievalService;
    private final PromptTemplateService promptTemplateService;
    private final AiModelService aiModelService;
    private final EvaluationService evaluationService;
    private final AuditLoggingService auditLoggingService;

    public TaskExecutionService(
            TaskRetrievalService taskRetrievalService,
            TaskRepository taskRepository,
            RetrievalService retrievalService,
            PromptTemplateService promptTemplateService,
            AiModelService aiModelService,
            EvaluationService evaluationService,
            AuditLoggingService auditLoggingService) {
        this.taskRetrievalService = taskRetrievalService;
        this.taskRepository = taskRepository;
        this.retrievalService = retrievalService;
        this.promptTemplateService = promptTemplateService;
        this.aiModelService = aiModelService;
        this.evaluationService = evaluationService;
        this.auditLoggingService = auditLoggingService;
    }

    public AiTask execute(UUID taskId) {
        AiTask task = taskRetrievalService.retrieve(taskId);
        task.markExecuting();
        taskRepository.save(task);
        auditLoggingService.record(taskId, "task.execution.started", Map.of());

        List<String> context = retrievalService.retrieveContext(task);
        String prompt = promptTemplateService.engineeringReviewTemplate().render(task.payload(), context);

        try {
            String modelOutput = aiModelService.generate(prompt);
            EvaluationResult evaluation = evaluationService.evaluate(modelOutput, context);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("model_output", modelOutput);
            result.put("retrieval_context", context);
            result.put("evaluation", evaluation);
            task.markCompleted(result);
            taskRepository.save(task);
            retrievalService.indexResult(task, result);
            auditLoggingService.record(taskId, "task.execution.completed", Map.of(
                    "evaluation_label", evaluation.label(),
                    "evaluation_score", evaluation.score()));
            return task;
        } catch (TaskDependencyException exception) {
            task.markFailed(Map.of("error", exception.getMessage()));
            taskRepository.save(task);
            auditLoggingService.record(taskId, "task.execution.failed", Map.of("error", exception.getMessage()));
            throw exception;
        }
    }
}
