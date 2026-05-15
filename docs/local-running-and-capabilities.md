# Local Running and Capabilities Guide

## Overview

AI Engineering Enablement Portal is a Spring Boot application for submitting engineering tasks and routing them through a structured AI review workflow. It supports task creation, task retrieval, execution through a local AI model, lightweight retrieval, deterministic evaluation, human review gates, and audit logging.

The current implementation is intended for local development and early product validation. Task state, retrieval context, and audit events are stored in memory and are reset when the application restarts.

## Prerequisites

- Java 21
- Maven via the included wrapper or a local Maven install
- A local Ollama-compatible model server for task execution
- The configured model pulled locally, such as `llama3.1`

## Start the Local AI Model

Install and start Ollama, then pull the configured model:

```powershell
ollama pull llama3.1
ollama serve
```

The application defaults to:

```yaml
ai:
  local:
    base-url: http://localhost:11434
    model: llama3.1
    timeout-seconds: 60
```

Override those values in `src/main/resources/application.yaml`, with Spring environment variables, or with command-line properties:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--ai.local.model=llama3.1"
```

## Run the Application

From the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

If the Maven wrapper is unavailable in your shell, use a local Maven install:

```powershell
mvn spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Run Tests

```powershell
.\mvnw.cmd test
```

The tests cover application startup, health checks, task endpoint behavior, duplicate task rejection, dependency failure handling, and the Ollama-compatible local AI adapter contract.

## API Capabilities

The authoritative API contract is `docs/swagger.yaml`.

### Create a Task

Creates an engineering task with a client-provided UUID and flexible JSON payload.

```powershell
$taskId = [guid]::NewGuid().ToString()

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/tasks `
  -ContentType application/json `
  -Body "{
    `"task_id`": `"$taskId`",
    `"data`": {
      `"task_type`": `"code_review`",
      `"prompt`": `"Review this controller for validation and error handling gaps.`"
    }
  }"
```

Response status: `201 Created`

### List Tasks

Returns paginated task summaries.

```powershell
Invoke-RestMethod "http://localhost:8080/tasks?page_number=1&page_size=25"
```

### Retrieve a Task

Returns full task state, including payload, result, review, and timestamps.

```powershell
Invoke-RestMethod "http://localhost:8080/tasks/$taskId"
```

### Execute a Task

Runs the task through retrieval, structured prompt rendering, the local AI model, evaluation, result storage, and audit logging.

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/tasks/$taskId/execution"
```

Successful execution moves the task to `pending_review`.

If the local model is unavailable, the API returns `424 DEPENDENCY_FAILURE` and records the task as `failed`.

### Audit a Task

Records a human review decision and moves the task into the selected review state.

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/tasks/$taskId/audit" `
  -ContentType application/json `
  -Body '{
    "review_status": "approved",
    "review_comments": "Output meets the review criteria."
  }'
```

Allowed review statuses:

- `approved`
- `rejected`
- `needs_changes`

## Supported Workflow

The current task lifecycle is:

```text
created -> executing -> pending_review -> approved
                                      -> rejected
                                      -> needs_changes
```

Failure path:

```text
created -> executing -> failed
```

## Current Capabilities

- Swagger-aligned REST API for task creation, listing, retrieval, execution, and audit.
- In-memory task persistence for local development.
- In-memory retrieval over previous task outputs.
- Structured prompt construction for engineering review tasks.
- Real local AI model calls through an Ollama-compatible `/api/generate` endpoint.
- Deterministic output evaluation with score, label, and findings.
- Human review gate with review status and comments.
- In-memory audit logging for task creation, execution start, execution completion, execution failure, and review decisions.
- Error responses for invalid input, duplicate task IDs, missing tasks, dependency failures, and unexpected server errors.

## Known Limitations

- Data is not durable across application restarts.
- Authentication and authorization are not implemented yet, although the OpenAPI contract reserves `401` and `403` responses.
- Retrieval is token-based and in-memory; production retrieval should use durable full-text or vector search.
- Audit logs are in-memory; production audit events should be immutable and durable.
- Task payloads are flexible JSON; production task types should get stricter schemas.
- Local model latency, token usage, and quality metrics are not yet exported as application metrics.

## Troubleshooting

### Maven Wrapper Does Not Start

If `.\mvnw.cmd` fails in PowerShell, try running it from `cmd.exe` or use a local Maven installation:

```powershell
mvn test
```

### Execution Returns `424 DEPENDENCY_FAILURE`

Check that Ollama is running and that the configured model is available:

```powershell
ollama list
ollama pull llama3.1
```

Then verify the model endpoint:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:11434/api/generate `
  -ContentType application/json `
  -Body '{"model":"llama3.1","prompt":"Say hello","stream":false}'
```

### Duplicate Task Returns `409 CONFLICT`

`POST /tasks` is intentionally not idempotent. Generate a new UUID for each new task submission.
