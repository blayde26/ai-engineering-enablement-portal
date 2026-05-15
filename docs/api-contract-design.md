# AI Engineering Enablement Portal API Design

## Task Understanding

The application lets developers submit engineering work items such as code review requests, test generation requests, security reviews, architecture critiques, documentation questions, and incident summaries. The monolith stores the task in memory, routes execution through structured prompting, in-memory retrieval, a local AI model endpoint, deterministic evaluation, human review gates, and audit logging.

The product goal is to create a repeatable review workflow around AI-assisted engineering work instead of letting one-off prompts disappear without traceability.

The least clear production concern is the local model runtime. The implementation assumes an Ollama-compatible HTTP API at `ai.local.base-url`, but teams must still choose the deployed model, resource limits, timeout policy, and authorization boundary.

Naive implementation risks include inconsistent API paths, unreviewed model output, no audit trail, unbounded task payloads, and execution behavior that silently succeeds without a real AI dependency.

## Clarifying Questions

Resolved decisions:

- Use `docs/swagger.yaml` as the authoritative API contract.
- Use in-memory persistence for the first implementation.
- Integrate with a real local AI model rather than returning static stub output.

Remaining questions:

- Which local model should be the default for each engineering task type?
- Should task payloads remain flexible JSON, or should each task type get a stricter schema?
- When authentication is added, which roles can execute tasks versus approve human review gates?

## Assumptions

### Functional Assumptions

- A task starts in `created`.
- Executing a task sends a structured prompt to a local AI model.
- Successful execution moves the task to `pending_review`.
- Human review moves the task to `approved`, `rejected`, or `needs_changes`.

### Technical Assumptions

- Spring Boot remains the application framework.
- Java 21 remains the runtime.
- The local model supports the Ollama `/api/generate` request and response shape.

### Data Assumptions

- `task_id` is a client-provided UUID.
- `data` is a required JSON object.
- Review comments are required and limited to 5000 characters.

### Operational Assumptions

- In-memory state is acceptable for local development and early review.
- Local AI model unavailability is a dependency failure, not a successful degraded execution.
- Audit logging is in-memory for now and should move to durable storage before production.

## Acceptance Criteria

- Given a valid task request, when the client posts to `POST /tasks`, then the API returns `201` with `status=success` and the `task_id`.
- Given a duplicate `task_id`, when the client posts to `POST /tasks`, then the API returns `409`.
- Given a known task, when the client gets `GET /tasks/{task_id}`, then the API returns the task payload, status, timestamps, result, and review if present.
- Given an unknown task, when the client retrieves, executes, or audits it, then the API returns `404`.
- Given a known task and an available local model, when the client posts to `POST /tasks/{task_id}/execution`, then the API invokes retrieval, prompting, AI generation, evaluation, audit logging, and moves the task to `pending_review`.
- Given local model failure, when execution is requested, then the API records failure state and returns `424`.
- Given a valid audit request, when the client posts to `POST /tasks/{task_id}/audit`, then the API records review status, comments, timestamp, and audit event.

## API Contract

The OpenAPI contract is maintained in `docs/swagger.yaml`.

### Endpoints

- `POST /tasks`
- `GET /tasks`
- `GET /tasks/{task_id}`
- `POST /tasks/{task_id}/execution`
- `POST /tasks/{task_id}/audit`

### Validation Rules

- `task_id` must be a UUID.
- `data` is required and must not be empty.
- `page_number` must be at least 1.
- `page_size` must be between 1 and 100.
- `review_status` must be `approved`, `rejected`, or `needs_changes`.
- `review_comments` must be nonblank and no longer than 5000 characters.

### Status Codes

- `201`: task created.
- `200`: task listed, retrieved, executed, or audited successfully.
- `400`: malformed JSON, invalid UUID, invalid enum, or validation failure.
- `404`: task not found.
- `409`: duplicate task or invalid state conflict.
- `424`: local AI, retrieval, or evaluation dependency failure.
- `500`: unexpected server error.

### Idempotency

`POST /tasks` is not idempotent. Reusing the same `task_id` returns `409` to protect client intent.

## Design Proposal

### Components

- `TaskController`: REST endpoints matching the Swagger contract.
- `TaskCreationService`: creates tasks and records creation audit events.
- `TaskRetrievalService`: retrieves a task by UUID.
- `TaskListingService`: paginates in-memory tasks.
- `TaskExecutionService`: orchestrates retrieval, prompt rendering, local model invocation, evaluation, result persistence, and audit events.
- `TaskAuditService`: records human review decisions.
- `LocalAiModelService`: calls an Ollama-compatible local model.
- `InMemoryRetrievalService`: indexes previous task outputs and returns matching context.
- `EvaluationService`: scores generated output for basic completeness and context use.
- `AuditLoggingService`: stores in-memory audit events.

### Data Flow

1. Client creates a task.
2. Client requests execution.
3. Execution service retrieves context from prior indexed outputs.
4. Prompt template renders payload and context.
5. Local AI model generates output.
6. Evaluation service scores output.
7. Task result and audit event are recorded.
8. Human reviewer audits the result.

### State Changes

`created -> executing -> pending_review -> approved|rejected|needs_changes`

Failure path:

`created|executing -> failed`

### Dependencies

- Spring MVC
- Bean Validation
- Local Ollama-compatible model endpoint
- In-memory concurrent collections

### Persistence Needs

Current persistence is in-memory. Production should move tasks, results, reviews, retrieval indexes, and audit events to durable storage.

### Concurrency Considerations

The repository uses concurrent collections. This is acceptable for a local prototype, but production needs transaction boundaries, optimistic locking, and durable audit writes.

### Performance Considerations

In-memory listing sorts all tasks and is acceptable only for small volumes. Retrieval is simple token matching over indexed results and should be replaced with vector or full-text retrieval when task volume grows.

### Security Considerations

Authentication and authorization are represented in the Swagger error contract but not implemented yet. Before production, callers should be authenticated, task payloads should be size-limited, sensitive data should be redacted from prompts and audit logs, and local model access should be network-restricted.

### Observability Considerations

Audit events capture business actions in memory. Production should add structured logs, request correlation IDs, model latency metrics, model failure counts, and review outcome metrics.

## Test Cases

| Name | Purpose | Input | Expected Result |
| --- | --- | --- | --- |
| `createRetrieveListAndAuditTask` | Verify the happy path for core task lifecycle endpoints | Valid task JSON and audit JSON | `201`, `200`, task status updates, audit success |
| `createTaskRejectsDuplicateTaskId` | Protect duplicate client IDs | Same UUID posted twice | First request `201`, second request `409` |
| `executeTaskReturnsDependencyFailureWhenLocalModelUnavailable` | Verify real local model failures are surfaced | Existing task with unavailable model URL | `424` and task marked failed |
| `retrieveTaskReturnsNotFound` | Verify missing task handling | Unknown UUID | `404` |

## Error Cases

- Invalid UUID returns `400`.
- Missing or empty `data` returns `400`.
- Duplicate task creation returns `409`.
- Missing task returns `404`.
- Local AI model timeout or connection failure returns `424`.
- Empty model response returns `424`.
- Invalid review status returns `400`.
- Missing review comments returns `400`.

## Risks and Tradeoffs

| Risk | Impact | Mitigation | Open Question |
| --- | --- | --- | --- |
| In-memory persistence | Data loss on restart | Keep scope local; move to database before production | Which database should be used? |
| Flexible payload schema | Weak validation and uneven prompt quality | Add task-type-specific schemas | Which task types are first-class? |
| Local model dependency | Execution fails when model is unavailable | Return `424`, mark task failed, log audit event | Which model/runtime is required in dev? |
| Simple retrieval | Low recall and no semantic search | Replace with vector/full-text retrieval later | What source corpus should retrieval use? |
| No auth yet | Unauthorized task access in shared environments | Add Spring Security before deployment | What identity provider should be used? |
| In-memory audit | Audit trail is not durable | Persist immutable audit records | What retention policy is required? |

## Implementation Plan

1. Implement REST endpoints from `docs/swagger.yaml`.
2. Add in-memory task repository.
3. Add task lifecycle model, review model, and response DTOs.
4. Add local AI model adapter.
5. Add prompt rendering, retrieval, evaluation, and audit services.
6. Add endpoint tests for creation, retrieval, listing, audit, conflict, not found, and dependency failure.
7. Defer durable persistence, authentication, task-specific schemas, and vector retrieval until the core workflow is validated.

## Final Review

The implemented design satisfies the current local-development acceptance criteria and keeps production concerns visible. The main remaining assumptions are the local model runtime, durable storage choice, authentication model, and task-type schema strategy.
