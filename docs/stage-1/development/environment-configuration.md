# Environment Configuration

## 1. Principle

Configuration is externalized. Source code and container images must not depend on hardcoded secrets or machine-specific absolute paths.

## 2. Committed examples

The repository may contain:

```text
core/.env.example
backend/.env.example
frontend/.env.example
```

Example files contain variable names, safe development examples and documentation only.

## 3. Ignored local files

The following should be ignored:

```text
core/.env
core/.env.local
core/.env.production
*.local.env
backend/.env*
frontend/.env*
```

Negated `.gitignore` rules may allow `*.env.example` and `.env.example` files.

## 4. Stage boundaries

Stage 1 establishes the environment-file policy. Concrete PostgreSQL, Flyway, JWT, CORS, storage and scheduler variables are added in the stages that implement those features.

## 5. Secret handling

- local secrets remain in ignored files or IDE-local configuration,
- CI secrets are stored in the CI platform's secret store,
- production secrets are injected by the deployment environment,
- example values must not be reused in production,
- logs must not print secret values.

## 6. Frontend variables

Only variables intentionally exposed to the browser may use the `VITE_` prefix. A `VITE_` variable is public after bundling and must never contain a secret.
