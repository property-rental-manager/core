# Repository Agent Instructions & Workflow Policy — Property Rental Manager

This repository root (`core/`) defines the source code, infrastructure, and process rules for **Property Rental Manager**.

---

## Repository Boundaries

- **Root Directory:** `core/`
- **Backend Module:** `core/backend/` (Spring Boot, Java 25)
- **Frontend Module:** `core/frontend/` (React, TypeScript, Vite)
- **Infrastructure Module:** `core/infra/` (Docker Compose, Proxy, Deployment)
- **Documentation Module:** `core/docs/` (Architecture, Decisions, Process, Stage Docs)
- **Scripts Directory:** `core/scripts/` (Development environment scripts)

Do **NOT** create nested root directories such as `property-rental-manager/` or `core/property-rental-manager-backend/`.

---

## MANDATORY STAGE DOCUMENTATION WORKFLOW

Starting from Stage 3 and continuing for **ALL** subsequent project stages, AI agents (Antigravity and any subsequent assistants) and project contributors **MUST** update project documentation immediately upon completing every stage.

Refer to the reusable [Stage Documentation Checklist](file:///home/admsuliga/dev/property-manager/core/docs/process/stage-documentation-checklist.md).

After every stage, you **MUST** perform the following 15 steps:

1. Update the stage status in the main [PROPERTY_RENTAL_MANAGER_IMPLEMENTATION_PLAN.md](file:///home/admsuliga/dev/property-manager/core/docs/PROPERTY_RENTAL_MANAGER_IMPLEMENTATION_PLAN.md).
2. Update the task checklist (`[x]`) for the completed stage in the implementation plan.
3. Complete the Stage Execution Record in the implementation plan.
4. Update the Progress Log table with the date, completed stage, summary of changes, and stage status.
5. Update the `Current Next Action` field to point to the next stage.
6. Update all technical documentation in `core/docs/` associated with the changes made.
7. Update `core/README.md` if startup commands, ports, or environment requirements changed.
8. Update API documentation (OpenAPI / Swagger / endpoints doc) if endpoints or request/response formats changed.
9. Update the ERD and data model documentation if database schemas changed.
10. Update the Decision Log in the implementation plan if new technical or domain decisions were accepted.
11. Update `core/CHANGELOG.md` under the `[Unreleased]` section with implemented changes.
12. Record actual test commands run and their real execution results (do not fabricate results).
13. Record unresolved issues, limitations, or technical debt remaining.
14. Do **NOT** mark a stage as `DONE` unless all acceptance criteria have been explicitly tested and verified.
15. Do **NOT** declare a command or test as executed if it was not actually run.
