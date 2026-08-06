# Changelog

All notable changes to the **Property Rental Manager** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **Stage 3 — Backend Foundation:**
  - Spring Boot YAML profiles (`development`, `production`, `test`).
  - Standardized `ApiErrorResponse` and `@RestControllerAdvice` global exception handling (`GlobalExceptionHandler`).
  - Request ID tracking via `X-Request-ID` header and `RequestIdFilter` with SLF4J MDC integration and guaranteed MDC cleanup.
  - Basic HTTP request duration logging via `RequestLoggingFilter`.
  - Jackson ISO-8601 date serialization configuration (`JacksonConfig`).
  - OpenAPI 3.1 & Swagger UI integration via Springdoc (`OpenApiConfig`, `/swagger-ui/index.html`).
  - Spring Boot Actuator `/actuator/health` probe configuration.
  - Generic paginated response wrapper `PageResponse<T>`.
  - Spring Data JPA Auditing via `BaseEntity` (`createdAt`, `updatedAt` `Instant` fields) and `JpaAuditingConfig`.
  - Central application `Clock` bean (`Clock.systemUTC()`) for deterministic time testing.
  - Comprehensive integration test suite using Testcontainers (`MockMvc` tests for error responses, request IDs, Actuator health, OpenAPI docs, JPA auditing, and pagination).
  - Mandatory 15-point stage documentation workflow policy ([AGENTS.md](file:///home/admsuliga/dev/property-manager/core/AGENTS.md) & [stage-documentation-checklist.md](file:///home/admsuliga/dev/property-manager/core/docs/process/stage-documentation-checklist.md)).
- **Stage 2 — Infrastructure & Database:**
  - PostgreSQL 17 Docker Compose setup with healthchecks, persistent volumes, and UTF-8 encoding.
  - Adminer tools profile (`--profile tools` on port 8081).
  - Flyway `V1__create_identity_tables.sql` migration for `users`, `roles`, `user_roles`, `user_profiles`.
  - Database management scripts (`dev-db-up.sh`, `dev-db-down.sh`, `dev-db-reset.sh`, `dev-db-logs.sh`).
