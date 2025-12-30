# System Architecture (v1)

## What we are building
A production-style app where users create Projects with Tasks and Dependencies.
The backend generates a valid schedule (and explanations) using graph-based logic.

Frontend (React) is responsible for UI and calling the API.
Backend (Spring Boot) is responsible for correctness, validation, scheduling algorithms, and persistence.

---

## High-level system design

### Components
1) React Client (UI)
- Collects input (tasks, dependencies, options)
- Calls backend endpoints
- Renders results (schedule timeline, metrics, warnings)
- Contains NO scheduling logic

2) Spring Boot API (Backend)
- Validates input
- Runs scheduling engine
- Saves and returns ScheduleRuns
- Owns authorization checks

3) Database (PostgreSQL)
- Stores Users, Projects, Tasks, Dependencies
- Stores ScheduleRuns (history) and ScheduledTasks

---

## Request flow (client ↔ server)

### Example: Generate schedule
1. User clicks “Generate”
2. React sends:
   POST /api/v1/projects/{projectId}/schedule-runs  (options in body)
3. Backend:
   - Loads tasks + dependencies from DB
   - Validates (cycle detection, bad durations, etc.)
   - Runs Scheduling Engine (pure algorithm)
   - Saves ScheduleRun + ScheduledTasks + Diagnostics
   - Returns JSON response
4. React:
   - Displays the schedule + metrics + warnings

---

## Backend layering (where each type of code belongs)

### Controller layer (HTTP boundary)
Responsibilities:
- Parse request DTOs
- Validate request format (required fields, types)
- Return response DTOs
Rules:
- No scheduling logic here
- No DB logic here besides calling services

### Service layer (business orchestration)
Responsibilities:
- Authorization (user owns project)
- Load/write DB data using repositories
- Call Scheduling Engine
- Persist results of runs
Rules:
- No direct HTTP concerns
- Keeps “workflow” logic

### Scheduling Engine (pure logic module)
Responsibilities:
- Take tasks + dependencies + options
- Produce schedule + metrics + diagnostics
Rules:
- No DB access
- No HTTP access
- Designed for unit tests

### Repository layer (data access)
Responsibilities:
- JPA repositories for entities
- Query helpers (by projectId, etc.)
Rules:
- No business rules
- No algorithm logic

---

## Scheduling logic (v1)
Inputs:
- Tasks: durationMinutes, dueAt (optional), priority
- Dependencies: directed edges (from -> to)
- Options: strategy, startAt

Steps:
1) Validate graph (cycle detection). If cycle -> FAIL with diagnostic.
2) Build an execution order (topological constraints).
3) Schedule tasks sequentially (single-worker MVP):
   - task start = max(currentTime, max(end of prerequisites))
   - task end = start + durationMinutes
4) Choose next task among available tasks using strategy:
   - default: EDD (earliest due date), tie-break MOST_BLOCKING

Outputs:
- ScheduledTasks (taskId, startAt, endAt, orderIndex)
- Metrics (late tasks, total lateness, critical path estimate)
- Diagnostics (missed deadline warnings, cycle error)

---

## System design concerns (production feel)

### Versioning
All endpoints under /api/v1 to avoid breaking clients later.

### Consistent errors
All failures return a consistent error JSON format so React can show errors cleanly.

### Auth boundary (v1.1)
- Users must only access their own projects.
- Initial MVP can be “single user dev mode”, but architecture assumes auth.

### Observability (v1.1)
- Request logging
- requestId / correlation id
- /actuator/health for service health

### Guardrails
- Limits: max tasks per project, max dependencies
- Reject requests that would be too large or slow

---

## Frontend structure (recommended)
client/src/
- api/ (API client wrapper)
- pages/ (routes)
- components/ (UI components)
- types/ (TypeScript types matching API)
- hooks/ (data fetching hooks)

Frontend rules:
- One API client module (no scattered fetch)
- UI renders backend output (no algorithm duplication)

---

## Implementation map (docs → code)
- api.md endpoints -> Spring Controllers
- data-model.md entities -> JPA Entities + migrations
- scheduling logic -> engine package + unit tests
