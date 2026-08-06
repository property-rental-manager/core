# Backend Foundation Architecture

This document describes the architectural design and core cross-cutting concerns of the **Property Rental Manager** backend foundation established in Stage 3.

---

## 1. Package Boundaries

Root package: `pl.propertyrentalmanager`

```text
pl.propertyrentalmanager
├── common/
│   ├── error/          # Error codes, DTOs, GlobalExceptionHandler
│   ├── exception/      # Shared domain exception base classes
│   ├── pagination/     # PageResponse generic wrapper
│   ├── persistence/    # BaseEntity mapped superclass with JPA auditing
│   ├── time/           # Clock configuration and time utilities
│   └── web/            # RequestIdFilter and RequestLoggingFilter
├── config/             # Jackson, OpenAPI, Security, and JPA Auditing beans
└── user/               # Identity & User entities (foundation stage)
```

---

## 2. HTTP Request Lifecycle

1. **`RequestIdFilter` (Order: HIGHEST_PRECEDENCE):**
   Extracts or generates `X-Request-ID`, sets MDC `requestId`, sets response header, attaches to HttpServletRequest attributes.
2. **`RequestLoggingFilter` (Order: 10):**
   Logs method, URI, status code, and execution duration. Excludes sensitive headers, body contents, and credentials.
3. **`SecurityConfig`:**
   Enforces stateless session management, permits public endpoints (`/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`), and prepares for Stage 4 JWT security.
4. **Controller & Business Logic:**
   Services use injected `Clock` for time-sensitive logic.
5. **`GlobalExceptionHandler` (`@RestControllerAdvice`):**
   Intercepts exceptions, maps them to structured `ApiErrorResponse` payloads with MDC `requestId`, and returns appropriate HTTP status codes without leaking stack traces.
6. **MDC Cleanup:**
   `RequestIdFilter` clears MDC in a `finally` block to prevent thread pool MDC leaks.

---

## 3. Pagination Strategy (`PageResponse<T>`)

Spring Data `Page<T>` is never exposed directly in public API contracts. Controllers map internal `Page<T>` instances to `PageResponse<T>`:

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean empty
)
```

---

## 4. Time & Auditing Strategy

- **Technical Timestamps:** Represented using `Instant` (UTC).
- **Application Clock:** Central `Clock` bean (`Clock.systemUTC()`) injected into services for deterministic unit testing (`Clock.fixed(...)`).
- **JPA Auditing:** Handled via `BaseEntity` mapped superclass and `JpaAuditingConfig` bound to the application `Clock`.

---

## 5. Environment Profiles & Security Boundaries

- **`development`:** Verbose logging, full health check details, active Swagger UI.
- **`production`:** Restricted health details, disabled Swagger UI / OpenAPI JSON, externalized secrets.
- **`test`:** Testcontainers PostgreSQL dynamic connection, MockMvc integration tests.
