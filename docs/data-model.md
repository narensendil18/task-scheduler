# Data Model (v1)

## Goal
Store enough data to:
- model tasks + dependencies as a directed graph per project
- run the scheduling engine
- store schedule results and history (ScheduleRuns)

---

## Tables / Entities

### users
- id (UUID, PK)
- email (unique, not null)
- password_hash (not null)
- created_at

### projects
- id (UUID, PK)
- owner_id (UUID, FK -> users.id, not null)
- name (not null)
- description (nullable)
- created_at
- updated_at

Index:
- (owner_id)

### tasks
- id (UUID, PK)
- project_id (UUID, FK -> projects.id, not null)
- title (not null)
- description (nullable)
- duration_minutes (int, not null, > 0)
- due_at (timestamp, nullable)
- priority (int, not null, default 3)
- created_at
- updated_at

Indexes:
- (project_id)
- (project_id, due_at)

### dependencies
Represents a directed edge: from_task -> to_task
- id (UUID, PK)
- project_id (UUID, FK -> projects.id, not null)
- from_task_id (UUID, FK -> tasks.id, not null)
- to_task_id (UUID, FK -> tasks.id, not null)
- created_at

Constraints:
- from_task_id != to_task_id
- unique(project_id, from_task_id, to_task_id)

Indexes:
- (project_id)
- (project_id, from_task_id)
- (project_id, to_task_id)

### schedule_runs
A saved execution of the engine.
- id (UUID, PK)
- project_id (UUID, FK -> projects.id, not null)
- created_by (UUID, FK -> users.id, not null)
- strategy (text/enum: EDD, PRIORITY, MOST_BLOCKING)
- start_at (timestamp, not null)
- status (text/enum: SUCCESS, FAILED)
- created_at

Indexes:
- (project_id, created_at desc)

### scheduled_tasks
Stores the output schedule for a run.
- id (UUID, PK)
- run_id (UUID, FK -> schedule_runs.id, not null)
- task_id (UUID, FK -> tasks.id, not null)
- start_at (timestamp, not null)
- end_at (timestamp, not null)
- order_index (int, not null)

Constraints:
- unique(run_id, task_id)

Indexes:
- (run_id)
- (run_id, order_index)

### run_diagnostics (recommended)
- id (UUID, PK)
- run_id (UUID, FK -> schedule_runs.id, not null)
- severity (text/enum: INFO, WARNING, ERROR)
- code (text)
- message (text)
- created_at

Index:
- (run_id)

---

## Relationship summary
- users 1 -> N projects
- projects 1 -> N tasks
- projects 1 -> N dependencies
- projects 1 -> N schedule_runs
- schedule_runs 1 -> N scheduled_tasks
- schedule_runs 1 -> N run_diagnostics
