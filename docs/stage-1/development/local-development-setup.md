# Local Development Setup

## 1. Supported workstation model

The primary development setup is:

```text
Windows
└── WSL Ubuntu
    ├── Git
    ├── Java
    ├── Maven Wrapper
    ├── Node.js and npm
    └── project files

Docker Desktop
└── WSL integration

IntelliJ IDEA on Windows
└── opens project and toolchains from WSL
```

The repository should remain in the WSL Linux filesystem rather than under `/mnt/c`, because JavaScript dependency installation and Java builds are generally more predictable and performant there.

## 2. Repository location

Documented repository/project root:

```bash
~/dev/property-rental-manager/core
```

The outer `~/dev/property-rental-manager` directory is only a local parent/workspace. The Git project begins at `core/`.

Backend module:

```bash
~/dev/property-rental-manager/core/backend
```

Frontend module:

```bash
~/dev/property-rental-manager/core/frontend
```

Infrastructure, documentation and scripts:

```bash
~/dev/property-rental-manager/core/infra
~/dev/property-rental-manager/core/docs
~/dev/property-rental-manager/core/scripts
```

Relative to the repository root, these paths are simply `backend/`, `frontend/`, `infra/`, `docs/` and `scripts/`.

## 3. Required tools

- WSL Ubuntu,
- Git,
- Java 25,
- Maven Wrapper from the repository,
- Node.js and npm,
- Docker Desktop with WSL integration,
- IntelliJ IDEA.

Verify tools:

```bash
java --version
node --version
npm --version
git --version
docker --version
docker compose version
```

Verify Maven from the backend module:

```bash
cd ~/dev/property-rental-manager/core/backend
./mvnw --version
```

## 4. Clone and initialize

```bash
mkdir -p ~/dev/property-rental-manager
cd ~/dev/property-rental-manager
git clone <repository-url> core
cd core
```

Do not place credentials in the clone command, Git remote or committed files.

## 5. Backend workflow

```bash
cd ~/dev/property-rental-manager/core/backend
./mvnw clean verify
./mvnw spring-boot:run
```

At Stage 1, the backend only needs to prove that the project builds and starts. PostgreSQL, Flyway and the final development profile belong to Stage 2 and later.

## 6. Frontend workflow

```bash
cd ~/dev/property-rental-manager/core/frontend
npm ci
npm run lint
npm run build
npm run dev
```

Use `npm ci` whenever the lockfile is present and an exact repeatable installation is required.

## 7. IntelliJ IDEA configuration

Open the project using its WSL location, for example:

```text
\\wsl.localhost\Ubuntu\home\<user>\dev\property-rental-manager\core
```

Recommended configuration:

- Project SDK: WSL Java 25,
- backend Maven: repository Maven Wrapper,
- frontend Node interpreter: WSL Node.js,
- terminal shell: WSL Ubuntu,
- backend working directory: backend module directory,
- frontend working directory: frontend module directory.

Run configurations may refer to local environment variables, but credentials must not be stored in shared project files.

## 8. Common checks

From the repository root:

```bash
git status
find . -maxdepth 3 -type d \( -name target -o -name node_modules -o -name dist \)
```

Generated directories may exist locally but must remain ignored.
