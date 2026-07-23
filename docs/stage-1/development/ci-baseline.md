# CI Baseline

## 1. Objective

The Stage 1 CI baseline uses paths relative to the repository root `core/` and proves that a clean repository state can install dependencies, compile and run the project's available validation commands.

## 2. Trigger policy

CI should run for:

- pull requests targeting the stable or integration branch,
- pushes to `main`,
- pushes to `develop` when that branch is used.

## 3. Backend job

Minimum steps:

1. checkout repository,
2. install the required Java distribution,
3. enable Maven dependency cache,
4. make Maven Wrapper executable,
5. run:

```bash
cd backend
./mvnw --batch-mode clean verify
```

## 4. Frontend job

Minimum steps:

1. checkout repository,
2. install the repository's supported Node.js version,
3. enable npm cache using the frontend lockfile,
4. run:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

Run frontend tests when a non-interactive test script exists.

## 5. Failure policy

A pull request is not ready to merge when:

- backend compilation fails,
- backend tests fail,
- frontend dependency installation fails,
- frontend lint fails,
- frontend production build fails.

## 6. Security policy

- secrets must be referenced from protected CI secret storage,
- CI logs must not print secret values,
- workflow files must pin or deliberately manage third-party action versions,
- untrusted pull requests must not receive production secrets.
