# Stage 1 Verification Checklist

Use this checklist after cloning the repository on a clean workstation or when reviewing Stage 1 later.

## 1. Repository

- [ ] The `core/` directory opens as the repository root without missing submodules.
- [ ] `backend` exists.
- [ ] `frontend` exists.
- [ ] `infra`, `docs`, and `scripts` directories exist or are intentionally empty placeholders.
- [ ] `.gitignore`, `.editorconfig`, `README.md`, `CHANGELOG.md`, and `.env.example` exist in `core/` when applicable.
- [ ] No generated directories such as `target`, `node_modules`, or `dist` are tracked.
- [ ] No `.env`, private key, token, password, certificate private key, or IDE-local secret is tracked.

## 2. Toolchain

Run from WSL:

```bash
java --version
./backend/mvnw --version
node --version
npm --version
git --version
docker --version
docker compose version
```

- [ ] Java resolves from WSL.
- [ ] Maven Wrapper runs successfully.
- [ ] Node.js and npm resolve from WSL.
- [ ] Docker commands can reach Docker Desktop.
- [ ] Git uses the expected user name and email.

## 3. Backend

```bash
cd ~/dev/property-rental-manager/core/backend
./mvnw clean verify
./mvnw spring-boot:run
```

- [ ] Maven dependencies resolve.
- [ ] Tests pass.
- [ ] The application starts without an IDE-only configuration.
- [ ] The process can be stopped cleanly.

## 4. Frontend

```bash
cd ~/dev/property-rental-manager/core/frontend
npm ci
npm run lint
npm run build
npm run dev
```

- [ ] Dependencies install from the lockfile.
- [ ] Lint passes.
- [ ] Production build succeeds.
- [ ] Development server starts.
- [ ] The browser can open the local development URL printed by Vite.

## 5. IDE

- [ ] IntelliJ opens the repository through the WSL path.
- [ ] Project SDK points to the WSL JDK.
- [ ] Maven uses the wrapper and WSL environment.
- [ ] Node interpreter points to WSL.
- [ ] Terminal opens in the repository directory inside WSL.
- [ ] Run configurations do not contain committed credentials.

## 6. CI

- [ ] A clean push or pull request triggers backend validation.
- [ ] A clean push or pull request triggers frontend install, lint, test where available, and build.
- [ ] CI fails on a backend compilation/test failure.
- [ ] CI fails on a frontend lint/build failure.
- [ ] Dependency caches do not bypass lockfile or wrapper validation.
