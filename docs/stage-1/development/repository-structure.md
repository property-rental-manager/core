# Repository Structure

## 1. Repository model

Property Rental Manager uses a single monorepo whose root directory is named `core`. Application code, infrastructure configuration, documentation, scripts and repository-level configuration are versioned directly inside this root.

## 2. Repository structure

```text
core/
├── backend/
├── frontend/
├── infra/
│   ├── nginx/
│   └── docker/
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── decisions/
│   ├── deployment/
│   ├── development/
│   └── requirements/
├── scripts/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── pull_request_template.md
│   └── workflows/
├── compose.yaml
├── compose.dev.yaml
├── .env.example
├── .editorconfig
├── .gitignore
├── README.md
└── CHANGELOG.md
```

## 3. Directory responsibilities

### `backend`

Spring Boot application containing the REST API and, in later stages, authentication, properties, tenancies, invoices, files, notifications, administration and audit modules.

The backend directory owns:

- `pom.xml` and Maven Wrapper,
- Java source code,
- backend tests,
- Spring configuration,
- Flyway migrations after Stage 2,
- backend-specific Dockerfile after the deployment stage.

### `frontend`

React + TypeScript + Vite application.

The frontend directory owns:

- `package.json` and lockfile,
- TypeScript configuration,
- Vite configuration,
- frontend source code and tests,
- ESLint configuration,
- frontend-specific Dockerfile after the deployment stage.

### `infra`

Infrastructure-only files. No domain business logic belongs here.

Expected subareas:

- reverse-proxy configuration,
- Docker Compose support files,
- container scripts,
- deployment templates.

### `docs`

Project source-of-truth documentation.

Changes to business rules, architecture or implementation stages must be reflected here before or together with code changes.

### `scripts`

Repeatable developer and operational commands, for example startup, reset, backup and restore scripts. Scripts must not embed credentials.

### `.github`

Repository automation, issue templates, pull-request templates and CI workflows.

## 4. Naming rules

- directories use lowercase kebab-case where practical,
- Java packages use lowercase dotted names,
- React components use PascalCase,
- TypeScript non-component modules use the established project convention,
- environment example files may be committed; real environment files may not,
- generated directories are never committed.

## 5. Architectural boundary

The `core/` directory is the actual repository and project root. It is not a wrapper around another repository root. It does not create a separate runtime service and does not alter the modular-monolith decision for the Spring Boot backend.
