package com.example.ai_engineering_enablement_portal.task.Service;

import com.example.ai_engineering_enablement_portal.agent.AgentFeedback;
import com.example.ai_engineering_enablement_portal.agent.AgentFeedbackPhase;
import com.example.ai_engineering_enablement_portal.agent.AgentProfile;
import com.example.ai_engineering_enablement_portal.agent.AgentProfileService;
import com.example.ai_engineering_enablement_portal.ai.AiModelService;
import com.example.ai_engineering_enablement_portal.audit.AuditLoggingService;
import com.example.ai_engineering_enablement_portal.eval.EvaluationResult;
import com.example.ai_engineering_enablement_portal.eval.EvaluationService;
import com.example.ai_engineering_enablement_portal.prompt.PromptTemplate;
import com.example.ai_engineering_enablement_portal.prompt.PromptTemplateService;
import com.example.ai_engineering_enablement_portal.retrieval.RetrievalService;
import com.example.ai_engineering_enablement_portal.task.AiTask;
import com.example.ai_engineering_enablement_portal.task.Exception.TaskDependencyException;
import com.example.ai_engineering_enablement_portal.task.Repository.TaskRepository;
import java.time.Instant;
import java.util.ArrayList;
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
    private final AgentProfileService agentProfileService;

    public TaskExecutionService(
            TaskRetrievalService taskRetrievalService,
            TaskRepository taskRepository,
            RetrievalService retrievalService,
            PromptTemplateService promptTemplateService,
            AiModelService aiModelService,
            EvaluationService evaluationService,
            AuditLoggingService auditLoggingService,
            AgentProfileService agentProfileService) {
        this.taskRetrievalService = taskRetrievalService;
        this.taskRepository = taskRepository;
        this.retrievalService = retrievalService;
        this.promptTemplateService = promptTemplateService;
        this.aiModelService = aiModelService;
        this.evaluationService = evaluationService;
        this.auditLoggingService = auditLoggingService;
        this.agentProfileService = agentProfileService;
    }

    public AiTask execute(UUID taskId) {
        return analyze(taskId, false);
    }

    public AiTask reanalyze(UUID taskId, boolean agentCollaboration) {
        return analyze(taskId, agentCollaboration);
    }

    private AiTask analyze(UUID taskId, boolean agentCollaboration) {
        AiTask task = taskRetrievalService.retrieve(taskId);
        task.markExecuting();
        taskRepository.save(task);
        auditLoggingService.record(taskId, "task.analysis.started", Map.of("agent_collaboration", agentCollaboration));

        UUID analysisRunId = UUID.randomUUID();
        List<String> context = retrievalService.retrieveContext(task);
        List<AgentProfile> profiles = agentProfileService.profilesFor(task.payload());
        PromptTemplate template = promptTemplateService.agentAnalysisTemplate();
        List<AgentFeedback> generatedFeedback = new ArrayList<>();

        try {
            List<AgentFeedback> initialFeedback = collectFeedback(
                    task,
                    analysisRunId,
                    profiles,
                    template,
                    context,
                    List.of(),
                    AgentFeedbackPhase.INITIAL_ANALYSIS,
                    "Analyze the task from your role. Identify concrete findings, risks, and recommended next actions.");
            generatedFeedback.addAll(initialFeedback);

            if (agentCollaboration && profiles.size() > 1) {
                List<AgentFeedback> critiques = collectFeedback(
                        task,
                        analysisRunId,
                        profiles,
                        template,
                        context,
                        initialFeedback,
                        AgentFeedbackPhase.CRITIQUE,
                        "Read the other agents' initial feedback. Try to poke holes in assumptions, missed risks, weak recommendations, or incomplete acceptance criteria.");
                generatedFeedback.addAll(critiques);

                List<AgentFeedback> responses = collectFeedback(
                        task,
                        analysisRunId,
                        profiles,
                        template,
                        context,
                        critiques,
                        AgentFeedbackPhase.RESPONSE,
                        "Respond to the critiques from the other agents. Clarify, revise, or defend your position and call out any remaining disagreement.");
                generatedFeedback.addAll(responses);
            }

            String combinedFeedback = generatedFeedback.stream()
                    .map(AgentFeedback::content)
                    .reduce("", (left, right) -> left + "\n" + right);
            EvaluationResult evaluation = evaluationService.evaluate(combinedFeedback, context);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis_run_id", analysisRunId);
            result.put("agent_collaboration", agentCollaboration);
            result.put("agents", profiles.stream().map(AgentProfile::displayName).toList());
            result.put("feedback_count", generatedFeedback.size());
            result.put("retrieval_context", context);
            result.put("evaluation", evaluation);
            task.markCompleted(result);
            taskRepository.save(task);
            retrievalService.indexResult(task, result);
            auditLoggingService.record(taskId, "task.analysis.completed", Map.of(
                    "analysis_run_id", analysisRunId.toString(),
                    "agent_collaboration", agentCollaboration,
                    "feedback_count", generatedFeedback.size(),
                    "evaluation_label", evaluation.label(),
                    "evaluation_score", evaluation.score()));
            return task;
        } catch (TaskDependencyException exception) {
            task.markFailed(Map.of("error", exception.getMessage(), "analysis_run_id", analysisRunId));
            taskRepository.save(task);
            auditLoggingService.record(taskId, "task.analysis.failed", Map.of(
                    "analysis_run_id", analysisRunId.toString(),
                    "error", exception.getMessage()));
            throw exception;
        }
    }

    private List<AgentFeedback> collectFeedback(
            AiTask task,
            UUID analysisRunId,
            List<AgentProfile> profiles,
            PromptTemplate template,
            List<String> context,
            List<AgentFeedback> phaseFeedback,
            AgentFeedbackPhase phase,
            String phaseInstruction) {
        List<AgentFeedback> feedback = new ArrayList<>();
        for (AgentProfile profile : profiles) {
            List<AgentFeedback> feedbackForPrompt = phaseFeedback.stream()
                    .filter(existing -> existing.agentRole() != profile.role())
                    .toList();
            String prompt = template.render(
                    profile,
                    task.payload(),
                    context,
                    task.agentFeedback(),
                    feedbackForPrompt,
                    phaseInstruction);
            AgentFeedback agentFeedback = new AgentFeedback(
                    analysisRunId,
                    profile.role(),
                    profile.displayName(),
                    phase,
                    aiModelService.generate(prompt),
                    Instant.now());
            task.recordAgentFeedback(agentFeedback);
            taskRepository.save(task);
            feedback.add(agentFeedback);
        }
        return feedback;
    }
}
