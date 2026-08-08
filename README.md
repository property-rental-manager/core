# Property Rental Manager — Core Repository

Property Rental Manager is a platform for managing rental properties, tenancies, and invoices for Admins, Owners, and Tenants.

---

## Repository Structure

```text
core/
├── backend/          # Spring Boot Backend (Java 25)
├── frontend/         # React + TypeScript Frontend (Vite)
├── infra/            # Docker Compose & Infrastructure configuration
├── docs/             # Technical documentation & implementation plans
├── scripts/          # Development utility scripts
└── AGENTS.md         # Repository agent instructions & mandatory workflows
```

---

## Quick Start — Backend & Local Infrastructure

1. **Configure Environment:**
   ```bash
   cp .env.example .env
   ```

2. **Start PostgreSQL Database:**
   ```bash
   ./scripts/dev-db-up.sh
   ```

3. **Start Optional Adminer Tools (Web DB Client on port 8081):**
   ```bash
   docker compose --env-file .env -f infra/docker/compose.dev.yaml --profile tools up -d
   ```

4. **Run Backend (Development Profile with Admin Bootstrap):**
   ```bash
   cd backend
   APP_BOOTSTRAP_ADMIN_ENABLED=true \
   APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com \
   APP_BOOTSTRAP_ADMIN_PASSWORD=AdminPassword123! \
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=development
   ```

5. **Run Backend Integration Tests:**
   ```bash
   cd backend
   ./mvnw test
   ```

6. **Backend Endpoints & Developer Tools:**
   - **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   - **OpenAPI 3 JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
   - **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - **Adminer DB Client:** [http://localhost:8081](http://localhost:8081)

---

## Quick Start — Frontend Development

1. **Install Dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Run Frontend Dev Server:**
   ```bash
   cd frontend
   npm run dev
   ```
   Access application at: [http://localhost:5173](http://localhost:5173)

3. **Run Frontend Tests & Quality Checks:**
   ```bash
   cd frontend
   npm run lint
   npm run test
   npm run build
   ```

---

## Technical Documentation & Process Rules

- [Frontend Development Guide](file:///home/admsuliga/dev/property-manager/core/docs/development/frontend.md)
- [Frontend Foundation Architecture](file:///home/admsuliga/dev/property-manager/core/docs/architecture/frontend-foundation.md)
- [Backend Development Guide](file:///home/admsuliga/dev/property-manager/core/docs/development/backend.md)
- [Backend Foundation Architecture](file:///home/admsuliga/dev/property-manager/core/docs/architecture/backend-foundation.md)
- [Database Development Guide](file:///home/admsuliga/dev/property-manager/core/docs/development/database.md)
- [Authentication API Specification](file:///home/admsuliga/dev/property-manager/core/docs/api/authentication.md)
- [Security Architecture — Auth & Tokens](file:///home/admsuliga/dev/property-manager/core/docs/security/authentication-and-tokens.md)
- [Entity Relationship Diagram (ERD)](file:///home/admsuliga/dev/property-manager/core/docs/architecture/erd.md)
- [Stage Documentation Checklist](file:///home/admsuliga/dev/property-manager/core/docs/process/stage-documentation-checklist.md)
- [Repository Agent Rules (AGENTS.md)](file:///home/admsuliga/dev/property-manager/core/AGENTS.md)
