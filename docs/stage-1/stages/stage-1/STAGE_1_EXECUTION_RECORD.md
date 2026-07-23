# Stage 1 Execution Record

## Stage metadata

| Field | Value |
|---|---|
| Stage | 1 — Repository and development environment |
| Status | `DONE` |
| Completion date | 2026-07-23 |
| Completion confirmed by | Project owner |
| Main objective | Create a repeatable repository structure and local development workflow |
| Blocking issues | None declared |
| Next stage | Stage 2 — Local infrastructure and database foundation |

## Implemented scope

The following Stage 1 areas are recorded as completed:

- repository initialized as a monorepo whose root directory is named `core`,
- backend and frontend application modules created,
- infrastructure, documentation and scripts placed inside the repository root `core/`,
- `.gitignore` configured,
- `.editorconfig` configured,
- `.env.example` introduced,
- project startup documented in the root README,
- Git branch and commit conventions defined,
- editor and IDE workflow established,
- Java, Maven, Node.js, npm, Git and Docker toolchain established,
- frontend linting and formatting baseline established,
- basic CI expectations defined,
- secrets excluded from version control.

## As-built repository structure

```text
core/
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── src/
├── frontend/
│   ├── package.json
│   ├── src/
│   └── vite.config.ts
├── infra/
├── docs/
├── scripts/
├── .github/workflows/
├── .env.example
├── .editorconfig
├── .gitignore
├── README.md
└── CHANGELOG.md
```

Exact optional files may be added as later stages introduce Docker Compose, reverse proxy configuration and deployment scripts.

## Toolchain record

| Tool | Stage 1 approach |
|---|---|
| Operating environment | Windows + WSL Ubuntu |
| Project filesystem | WSL Linux filesystem |
| IDE | IntelliJ IDEA on Windows with WSL-based project/toolchain |
| Java | Java 25 in WSL |
| Backend build | Maven Wrapper (`./mvnw`) |
| Frontend runtime | Node.js and npm in WSL |
| Frontend framework | React + TypeScript + Vite |
| Containers | Docker Desktop with WSL integration |
| Version control | Git in WSL |

## Acceptance criteria result

| Acceptance criterion | Result |
|---|---|
| A developer can clone the repository into a directory named `core` and locate its direct children `backend`, `frontend`, `infra` and `docs` | Passed |
| Backend and frontend can be started using repository documentation | Passed by project-owner confirmation |
| Local tooling is available through the WSL environment | Passed |
| Repository includes baseline ignore and editor configuration | Passed |
| Environment examples do not contain production secrets | Passed |
| Generated build output and local secrets are excluded from Git | Passed |
| Basic validation can be run locally and in CI | Passed by project-owner confirmation |

## Known documentation limitation

This record documents Stage 1 based on the project owner's confirmation and known project structure. It is not a source-code audit or a CI-run report. The verification checklist in this package can be used whenever a clean-machine or repository audit is required.

## Definition of Done result

Stage 1 satisfies the planning-level Definition of Done and is marked `DONE`.
