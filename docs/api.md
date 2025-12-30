# API Contract (v1)

Base path: /api/v1
JSON in/out.

## Response conventions
- 2xx for success
- 4xx for client errors (validation, unauthorized)
- 5xx for server errors

## Standard error format
{
  "timestamp": "2025-12-23T22:10:00Z",
  "path": "/api/v1/...",
  "error": "VALIDATION_ERROR",
  "message": "Human readable message",
  "details": [
    { "field": "durationMinutes", "issue": "must be > 0" }
  ]
}

---

## Auth (v1.1)
MVP can be stubbed, but endpoints are planned.

POST /auth/register
POST /auth/login
GET  /me

Auth method (planned):
- JWT in Authorization: Bearer <token>

---

## Projects

### Create Project
POST /projects
Request:
{
  "name": "Spring Semester",
  "description": "Plan my workload"
}

Response 201:
{
  "id": "uuid",
  "name": "Spring Semester",
  "description": "Plan my workload",
  "createdAt": "...",
  "updatedAt": "..."
}

### List Projects
GET /projects
Response 200: [ { ...Project } ]

### Get Project
GET /projects/{projectId}
Response 200: { ...Project }

---

## Tasks

### Create Task
POST /projects/{projectId}/tasks
Request:
{
  "title": "Write report",
  "description": "Draft + revise",
  "durationMinutes": 180,
  "dueAt": "2026-01-10T23:59:00Z",
  "priority": 4
}
Response 201: { ...Task }

### List Tasks
GET /projects/{projectId}/tasks
Response 200: [ { ...Task } ]

### Update Task
PATCH /tasks/{taskId}
Request (partial):
{
  "durationMinutes": 240,
  "priority": 5
}
Response 200: { ...Task }

---

## Dependencies

### Add Dependency
POST /projects/{projectId}/dependencies
Request:
{
  "fromTaskId": "uuid",
  "toTaskId": "uuid"
}
Response 201:
{
  "id": "uuid",
  "fromTaskId": "uuid",
  "toTaskId": "uuid",
  "createdAt": "..."
}

Errors:
- VALIDATION_ERROR if fromTaskId == toTaskId
- VALIDATION_ERROR if edge already exists

### List Dependencies
GET /projects/{projectId}/dependencies
Response 200: [ { ...Dependency } ]

---

## Scheduling

### Create Schedule Run (Generate schedule)
POST /projects/{projectId}/schedule-runs
Request:
{
  "strategy": "EDD",
  "startAt": "2026-01-01T09:00:00Z"
}

Response 201 (SUCCESS):
{
  "runId": "uuid",
  "status": "SUCCESS",
  "strategy": "EDD",
  "metrics": {
    "totalTasks": 12,
    "lateTasks": 2,
    "totalLatenessMinutes": 180,
    "criticalPathMinutes": 420
  },
  "scheduledTasks": [
    {
      "taskId": "uuid",
      "startAt": "2026-01-01T09:00:00Z",
      "endAt": "2026-01-01T12:00:00Z",
      "orderIndex": 1
    }
  ],
  "diagnostics": [
    { "severity": "WARNING", "code": "MISSED_DEADLINE", "message": "Task X finishes after due date" }
  ]
}

Response 201 (FAILED - cycle):
{
  "runId": "uuid",
  "status": "FAILED",
  "diagnostics": [
    { "severity": "ERROR", "code": "CYCLE_DETECTED", "message": "Cycle found: A -> B -> C -> A" }
  ]
}

### List Schedule Runs
GET /projects/{projectId}/schedule-runs
Response 200:
[
  { "runId": "uuid", "createdAt": "...", "status": "SUCCESS", "strategy": "EDD" }
]

### Get Schedule Run Details
GET /schedule-runs/{runId}
Response 200: same shape as Create Schedule Run success/fail
