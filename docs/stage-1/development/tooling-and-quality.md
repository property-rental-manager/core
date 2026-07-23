# Tooling and Code Quality

## 1. Editor consistency

The repository-root `.editorconfig` defines the minimum cross-editor convention:

- UTF-8,
- LF line endings,
- final newline,
- removal of trailing whitespace where appropriate,
- consistent indentation.

Language-specific formatters may refine these settings but should not conflict with them.

## 2. Backend quality baseline

Backend validation should be runnable with:

```bash
./mvnw clean verify
```

The Maven Wrapper is the canonical Maven entry point. Developers and CI should not depend on a globally installed Maven version.

Recommended checks as the backend grows:

- compilation,
- unit tests,
- integration tests,
- code formatting check,
- static analysis,
- dependency vulnerability review.

## 3. Frontend quality baseline

The frontend uses ESLint rather than Oxlint.

Required commands should be available through `package.json`:

```bash
npm run lint
npm run build
```

As tests are introduced, add a non-interactive CI command such as:

```bash
npm run test -- --run
```

TypeScript strict mode should remain enabled unless a documented exception is approved.

## 4. Generated artifacts

Do not commit:

```text
backend/target output
frontend/node_modules
dist output
coverage output
IDE caches
local log files
```

## 5. Quality ownership

Each feature stage is responsible for extending tests and tooling where new risks appear. Stage 1 provides the common execution path; later stages add domain-specific checks.
