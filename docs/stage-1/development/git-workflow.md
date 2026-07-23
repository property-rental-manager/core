# Git Workflow

## 1. Branches

The repository uses the following baseline:

- `main` — stable branch,
- `develop` — integration branch when used by the team,
- `feature/<name>` — new functionality,
- `fix/<name>` — defect correction,
- `docs/<name>` — documentation-only changes,
- `chore/<name>` — tooling and maintenance.

A small team may work directly from feature branches into `main` until a permanent `develop` branch becomes operational. The branch policy must remain consistent and documented in the repository.

## 2. Example branches

```text
feature/authentication-foundation
feature/property-management
fix/tenant-invoice-visibility
docs/stage-1-documentation
chore/ci-baseline
```

## 3. Commit format

Use concise conventional-style commits:

```text
feat: add property creation endpoint
fix: prevent pre-tenancy invoice access
test: add invoice authorization tests
docs: document stage 1 environment
chore: configure frontend linting
refactor: centralize permission checks
```

Project documentation files are stored under `docs/` relative to the `core/` repository root; the `docs/<name>` prefix above refers only to the Git branch name.

## 4. Pull request expectations

A pull request should include:

- a clear purpose,
- linked backlog item when available,
- summary of changed areas,
- tests performed,
- database migration impact,
- security or authorization impact,
- screenshots for meaningful UI changes,
- documentation updates.

## 5. Merge requirements

Before merge:

- CI passes,
- no secret is present in the diff,
- generated files are not committed,
- required tests pass,
- documentation is current,
- branch is reviewed when team workflow requires review.

## 6. Protected information

Never commit:

- `.env` files with real values,
- passwords or JWT signing keys,
- private keys and certificates,
- database dumps containing personal data,
- IDE-local credentials,
- access tokens,
- production host secrets.
