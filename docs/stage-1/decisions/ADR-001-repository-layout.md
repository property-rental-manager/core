# ADR-001 — `core/` as the Repository and Project Root

- **Status:** Accepted
- **Date:** 2026-07-23
- **Stage:** 1

## Context

Generic planning examples used `property-rental-manager/` as the root directory and sometimes placed a `core/` directory below it. The implemented project does not use that logical nesting.

A local parent directory named `property-rental-manager` may exist in the developer workspace, but it is not the repository root.

## Decision

The directory named `core/` is the actual repository and project root.

Its direct structure is:

```text
core/
├── backend/
├── frontend/
├── infra/
├── docs/
├── scripts/
├── .github/
├── compose.yaml
├── compose.dev.yaml
├── .env.example
├── .editorconfig
├── .gitignore
├── README.md
└── CHANGELOG.md
```

Repository-relative paths therefore use:

```text
backend/
frontend/
infra/
docs/
scripts/
```

They do not use `core/backend` or `property-rental-manager/core/backend` when commands are already executed from the repository root.

## Consequences

### Positive

- `core/` is the single unambiguous working directory for the entire project;
- backend, frontend, infrastructure, documentation and scripts are direct project areas;
- repository-level Git, CI and environment files live in the same root;
- commands and CI working directories are shorter and clearer.

### Operational

- a typical local absolute path may be `~/dev/property-rental-manager/core`;
- after entering that directory, backend commands use `cd backend` and frontend commands use `cd frontend`;
- documentation examples must distinguish the local parent/workspace path from the repository root.

### Architectural impact

None. The Spring Boot backend remains a modular monolith and the React frontend remains a separate application module.
