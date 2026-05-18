# Local Running and Capabilities Guide

## Overview

AI Engineering Enablement Portal is a Spring Boot application for submitting engineering tasks and routing them through a structured AI review workflow. It supports task creation, task retrieval, execution through local AI agent profiles, lightweight retrieval, deterministic evaluation, agent collaboration, human review gates, and audit logging.

The current implementation is intended for local development and early product validation. Task state, agent feedback, retrieval context, and audit events are stored in memory and are reset when the application restarts.

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
- In-memory task persistence for local development.
- Task-type-based agent routing.
- Dedicated agent personas with role-specific system prompts.
- Agent feedback persisted on each task with run id, role, phase, content, and timestamp.
- Optional `agent_collaboration` flow with critique and response phases.
- In-memory retrieval over previous task outputs.
- Real local AI model calls through an Ollama-compatible `/api/generate` endpoint.
- Deterministic output evaluation with score, label, and findings.
- In-memory audit logging for task creation, analysis start/completion/failure, and review decisions.

## Known Limitations

- Data is not durable across application restarts.
- Authentication and authorization are not implemented yet.
- Retrieval is token-based and in-memory; production retrieval should use durable full-text or vector search.
- Audit logs are in-memory; production audit events should be immutable and durable.
- Agent routing is hard-coded and should eventually be configuration-driven.
- Collaboration can multiply model calls, so production should add budgets, concurrency limits, and cancellation.

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
