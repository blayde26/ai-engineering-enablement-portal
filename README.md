A Spring Boot application that lets developers submit engineering tasks such as code review requests, test generation requests, security reviews, architecture critiques, documentation questions, and incident summaries. The application routes those tasks through structured prompts, local AI models, retrieval, evaluation, human review gates, and audit logging.

## Local AI Model

Task execution calls an Ollama-compatible local model endpoint:

- `ai.local.base-url`: defaults to `http://localhost:11434`
- `ai.local.model`: defaults to `llama3.1`
- `ai.local.timeout-seconds`: defaults to `60`

Start Ollama locally and make sure the configured model is available before calling `POST /tasks/{task_id}/execution`. If the model is unavailable, the API records the task as failed and returns `424 DEPENDENCY_FAILURE`.

## Local Persistence

Tasks are saved to `data/tasks.json` by default so local task history survives application restarts. Override `storage.local.task-file-path` to store the file somewhere else.

Agent personas and task type routing are saved to `data/agent-routing.json` by default. Override `storage.local.agent-config-file-path` to store that configuration somewhere else.

## API

The API contract is documented in `docs/swagger.yaml`. The implemented task endpoints are:

- `POST /tasks`
- `GET /tasks`
- `GET /tasks/{task_id}`
- `POST /tasks/{task_id}/execution`
- `POST /tasks/{task_id}/analysis`
- `POST /tasks/{task_id}/audit`
- `GET /agent-profiles`
- `POST /agent-profiles`
- `GET /task-types`
- `POST /task-types`

For setup steps, example requests, supported capabilities, and troubleshooting, see `docs/local-running-and-capabilities.md`.
