# Backend Development Guide

This guide describes local development, environment configuration, testing, and architecture conventions for the **Property Rental Manager** Spring Boot backend (`core/backend`).

---

## 1. Environment & Requirements

- **Java Version:** Java 25 JDK
- **Build System:** Maven 3.9+ (via `./mvnw` wrapper)
- **Database:** PostgreSQL 17 (managed via Docker Compose)
- **Test Infrastructure:** Testcontainers (`postgres:17-alpine`)

---

## 2. Spring Boot Profiles

| Profile | Target Environment | Datasource Config | Actuator Health | OpenAPI / Swagger |
|---|---|---|---|---|
| `development` | Local developer machine | Defaults to `localhost:5432` | Detailed health (`always`) | Enabled (`/swagger-ui/index.html`) |
| `test` | Automated integration tests | Dynamic Testcontainers URL | Detailed health (`always`) | Enabled (`/v3/api-docs`) |
| `production` | Docker / Cloud Deployment | Secrets via Environment Variables | Status only (`never`) | Disabled |

---

## 3. Running Backend Locally

### Prerequisites
Start the PostgreSQL container:
```bash
./scripts/dev-db-up.sh
```

### From Command Line
Run with the `development` profile:
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
```

### From IntelliJ IDEA
1. Open `core/` in IntelliJ IDEA.
2. Ensure `core/backend/pom.xml` is imported as a Maven module.
3. Create a Spring Boot Run Configuration for `PropertyRentalManagerBackendApplication`.
4. Set Active Profiles to `development` (or VM Option: `-Dspring.profiles.active=development`).
5. Run or Debug the configuration.

---

## 4. Actuator Health & OpenAPI Endpoints

When running in `development` profile:

- **Health Endpoint:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **OpenAPI 3 JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 5. Request Tracking & Error Format

### Request ID (`X-Request-ID`)
Every HTTP request is processed through `RequestIdFilter`:
- Extracted from `X-Request-ID` header if valid, otherwise generated as a new UUID.
- Inserted into SLF4J MDC (`%X{requestId}`) for correlated log output.
- Included in the HTTP response header `X-Request-ID`.
- Included in all `ApiErrorResponse` payloads.
- Automatically cleared from MDC after request completion.

### Standard API Error Response (`ApiErrorResponse`)
All exceptions are intercepted by `GlobalExceptionHandler`:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    {
      "field": "email",
      "code": "NotBlank",
      "message": "Email is required"
    }
  ],
  "requestId": "f1e7efd0-18be-401a-aa1b-811cf72bc4ff",
  "timestamp": "2026-08-06T10:17:08.628Z",
  "path": "/api/v1/resource"
}
```

---

## 6. Time Management & JPA Auditing

- **Application `Clock` Bean:** Central `Clock` bean (`Clock.systemUTC()`) injected into services for time operations. Never use direct `Instant.now()` or `LocalDate.now()` calls in business code; use `Instant.now(clock)`.
- **Entity Auditing:** Entities extend `BaseEntity` (`@MappedSuperclass`) to automatically populate `createdAt` and `updatedAt` (`Instant`) via Spring Data JPA Auditing.

---

## 7. Running Integration Tests

Integration tests use Testcontainers and run a real PostgreSQL container:

```bash
cd backend
./mvnw test
```

---

## 8. Authentication & Admin Bootstrap

### Initial Admin Bootstrap
To bootstrap the initial administrator account on startup, pass the following environment variables or system properties:
```bash
APP_BOOTSTRAP_ADMIN_ENABLED=true \
APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com \
APP_BOOTSTRAP_ADMIN_PASSWORD=AdminPassword123! \
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
```

### Useful Authentication Curl Commands

1. **Fetch CSRF Token:**
   ```bash
   curl -i -c /tmp/cookies.txt http://localhost:8080/api/v1/auth/csrf
   ```

2. **Login:**
   ```bash
   curl -i -b /tmp/cookies.txt -c /tmp/cookies.txt \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@example.com","password":"AdminPassword123!"}' \
     http://localhost:8080/api/v1/auth/login
   ```

3. **Get Current User Profile:**
   ```bash
   curl -i -H "Authorization: Bearer <accessToken>" \
     http://localhost:8080/api/v1/me
   ```

4. **Rotate Refresh Token:**
   ```bash
   curl -i -b /tmp/cookies.txt -c /tmp/cookies.txt \
     -H "X-XSRF-TOKEN: <csrfToken>" \
     -X POST http://localhost:8080/api/v1/auth/refresh
   ```

5. **Change Password:**
   ```bash
   curl -i -H "Authorization: Bearer <accessToken>" \
     -H "Content-Type: application/json" \
     -d '{"currentPassword":"AdminPassword123!","newPassword":"NewSecurePassword123!"}' \
     http://localhost:8080/api/v1/me/password
   ```

6. **Logout:**
   ```bash
   curl -i -b /tmp/cookies.txt -c /tmp/cookies.txt \
     -H "X-XSRF-TOKEN: <csrfToken>" \
     -X POST http://localhost:8080/api/v1/auth/logout
   ```

