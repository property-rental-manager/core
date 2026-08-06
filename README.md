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

## Quick Start — Local Infrastructure & Backend

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

4. **Run Backend (Development Profile):**
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=development
   ```

5. **Run Integration Tests:**
   ```bash
   cd backend
   ./mvnw test
   ```

6. **Endpoints & Developer Tools:**
   - **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   - **OpenAPI 3 JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
   - **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - **Adminer DB Client:** [http://localhost:8081](http://localhost:8081)

---

## Technical Documentation & Process Rules

- [Database Development Guide](file:///home/admsuliga/dev/property-manager/core/docs/development/database.md)
- [Backend Development Guide](file:///home/admsuliga/dev/property-manager/core/docs/development/backend.md)
- [Backend Foundation Architecture](file:///home/admsuliga/dev/property-manager/core/docs/architecture/backend-foundation.md)
- [Stage Documentation Checklist](file:///home/admsuliga/dev/property-manager/core/docs/process/stage-documentation-checklist.md)
- [Repository Agent Rules (AGENTS.md)](file:///home/admsuliga/dev/property-manager/core/AGENTS.md)
