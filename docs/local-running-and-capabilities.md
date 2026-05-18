# Local Running and Capabilities Guide

## Overview

AI Engineering Enablement Portal is a Spring Boot application for submitting engineering tasks and routing them through a structured AI review workflow. It supports task creation, task retrieval, execution through local AI agent profiles, lightweight retrieval, deterministic evaluation, agent collaboration, human review gates, and audit logging.

The current implementation is intended for local development and early product validation. Task state, agent feedback, results, human reviews, agent personas, and task type routing are saved to local JSON files so they survive application restarts. Retrieval context and audit events remain in memory for now.

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

## Local Persistence

Tasks are saved to `data/tasks.json` by default:

```yaml
storage:
  local:
    task-file-path: data/tasks.json
    agent-config-file-path: data/agent-routing.json
```

These files are created automatically on first write. Delete them if you want to reset local task history or custom agent routing. Override the paths if you want the stores somewhere else:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--storage.local.task-file-path=C:/Users/brian/ai-portal/tasks.json --storage.local.agent-config-file-path=C:/Users/brian/ai-portal/agent-routing.json"
```

## Run the Application

From the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`.

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Run Tests

```powershell
.\mvnw.cmd test
```

## Agent Profiles

The first built-in profiles are:

- `Product Owner`: user value, scope clarity, acceptance criteria, prioritization, stakeholder impact, and delivery sequencing.
- `Principal Engineer`: correctness, maintainability, system design, scalability, operational risk, and implementation tradeoffs.
- `Test Engineer`: testability, missing tests, edge cases, regression risk, automation strategy, and measurable acceptance criteria.
- `Security Engineer`: threat modeling, abuse cases, data exposure, authorization, dependency risk, and secure-by-default mitigations.

Task type routing starts with these defaults:

- `user_story`, `documentation_question`, `incident_summary`: Product Owner and Principal Engineer.
- `work_plan`: Principal Engineer, Test Engineer, and Security Engineer.
- `test_generation`: Test Engineer and Principal Engineer.
- `security_review`: Security Engineer, Principal Engineer, and Test Engineer.
- `architecture_critique`: Principal Engineer and Security Engineer.
- `code_review`: Principal Engineer, Test Engineer, and Security Engineer.
- Unknown task types: Principal Engineer.

### Add a Custom Agent Profile

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/agent-profiles `
  -ContentType application/json `
  -Body '{
    "agent_id": "release_manager",
    "display_name": "Release Manager",
    "system_prompt": "Focus on release sequencing, rollback readiness, and stakeholder communication."
  }'
```

### Add a Custom Task Type Route

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/task-types `
  -ContentType application/json `
  -Body '{
    "task_type": "release_plan",
    "agent_ids": ["release_manager", "test_engineer"]
  }'
```

## API Capabilities

### Create a Task

```powershell
$taskId = [guid]::NewGuid().ToString()

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/tasks `
  -ContentType application/json `
  -Body "{
    `"task_id`": `"$taskId`",
    `"data`": {
      `"task_type`": `"work_plan`",
      `"prompt`": `"Create an implementation plan for adding audit exports.`"
    }
  }"
```

### List Tasks

```powershell
Invoke-RestMethod "http://localhost:8080/tasks?page_number=1&page_size=25"
```

### Retrieve a Task

Retrieval includes payload, result, review, timestamps, and documented `agent_feedback`.

```powershell
Invoke-RestMethod "http://localhost:8080/tasks/$taskId"
```

### Execute a Task

Runs first-pass analysis from the routed agent profiles.

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/tasks/$taskId/execution"
```

Successful execution moves the task to `pending_review`. If the local model is unavailable, the API returns `424 DEPENDENCY_FAILURE` and records the task as `failed`.

### Re-analyze a Task

Runs another analysis pass on an existing task. Set `agent_collaboration` to `true` to have agents critique each other and then respond to critiques.

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/tasks/$taskId/analysis" `
  -ContentType application/json `
  -Body '{
    "agent_collaboration": true
  }'
```

Agent collaboration phases are:

1. Initial analysis from each routed agent.
2. Critique, where each agent reviews the other agents' feedback and tries to identify holes.
3. Response, where each agent responds to the critiques they received.

### Audit a Task

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

Allowed review statuses are `approved`, `rejected`, and `needs_changes`.

## Current Capabilities

- Swagger-aligned REST API for task creation, listing, retrieval, execution, re-analysis, and audit.
- Local JSON-file task persistence for developer workstations.
- Local JSON-file agent persona and task type routing configuration.
- Task-type-based agent routing.
- Dedicated agent personas with role-specific system prompts.
- Agent feedback persisted on each task with run id, role, phase, content, and timestamp.
- Optional `agent_collaboration` flow with critique and response phases.
- In-memory retrieval over previous task outputs.
- Real local AI model calls through an Ollama-compatible `/api/generate` endpoint.
- Deterministic output evaluation with score, label, and findings.
- In-memory audit logging for task creation, analysis start/completion/failure, and review decisions.

## Known Limitations

- Retrieval context and audit events are not durable across application restarts.
- Authentication and authorization are not implemented yet.
- Retrieval is token-based and in-memory; production retrieval should use durable full-text or vector search.
- Audit logs are in-memory; production audit events should be immutable and durable.
- Built-in agent routing is seeded locally, and custom routes are stored in local JSON.
- Collaboration can multiply model calls, so production should add budgets, concurrency limits, and cancellation.
- The local JSON stores are not designed for multi-process access or production workloads.
- Custom system prompts are trusted local configuration; production should add authorization, review workflow, and prompt safety controls before exposing this broadly.

## Troubleshooting

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
