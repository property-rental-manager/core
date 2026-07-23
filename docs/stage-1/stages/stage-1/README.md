# Property Rental Manager — Stage 1 Documentation

> **Stage:** 1 — Repository and development environment  
> **Status:** `DONE`  
> **Completion basis:** completion confirmed by the project owner  
> **Documentation date:** 2026-07-23

## 1. Purpose

This package documents the repository structure, local development environment, Git workflow, environment-variable policy, code-quality tooling and CI baseline established during Stage 1.

Stage 1 provides the repeatable foundation required before database and infrastructure work begins in Stage 2.

## 2. Current project layout

The repository is maintained as a monorepo. The directory `core/` is the repository and project root. Its direct children include the backend, frontend, infrastructure, documentation and scripts:

```text
core/
├── backend/
├── frontend/
├── infra/
├── docs/
├── scripts/
├── .github/
│   └── workflows/
├── .env.example
├── .editorconfig
├── .gitignore
├── README.md
└── CHANGELOG.md
```

The use of `core/` is an accepted repository-organization decision. The folders `backend`, `frontend`, `infra`, `docs` and `scripts` are all located inside the repository root `core/`. This does not change the planned runtime architecture.

## 3. Development environment

The documented development environment is:

- Windows host with WSL Ubuntu,
- repository root stored in the Linux filesystem at `~/dev/property-rental-manager/core` (the outer `property-rental-manager` directory is only a local parent/workspace),
- IntelliJ IDEA running on Windows and opening the project from WSL,
- Java 25 available inside WSL,
- Maven executed through Maven Wrapper,
- Node.js and npm executed inside WSL,
- Docker Desktop with WSL integration,
- Git executed inside WSL.

## 4. Documents in this package

- `docs/stages/stage-1/STAGE_1_EXECUTION_RECORD.md` — completion record and acceptance result,
- `docs/stages/stage-1/STAGE_1_VERIFICATION_CHECKLIST.md` — repeatable verification checklist,
- `docs/development/repository-structure.md` — repository layout and ownership rules,
- `docs/development/local-development-setup.md` — workstation and startup guide,
- `docs/development/git-workflow.md` — branches, commits and pull requests,
- `docs/development/environment-configuration.md` — environment and secret-handling rules,
- `docs/development/tooling-and-quality.md` — formatting, linting and editor conventions,
- `docs/development/ci-baseline.md` — minimum CI workflow for Stage 1,
- `docs/decisions/ADR-001-repository-layout.md` — decision recording the `core/` repository-root layout.

## 5. Stage result

Stage 1 is complete. The repository and development environment are ready for:

> **Stage 2 — Local infrastructure and database foundation.**
