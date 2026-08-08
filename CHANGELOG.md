# Changelog

All notable changes to the **Property Rental Manager** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **Stage 5 — Frontend Foundation:**
  - Complete React 19 + TypeScript + Vite frontend architecture (`core/frontend/`).
  - Central `apiClient.ts` with memory-only JWT access token storage (`memoryAccessToken`), CSRF double-submit header injection (`X-XSRF-TOKEN`), and single-flight 401 refresh queue retry mechanism (`refreshSingleFlight`).
  - `AuthProvider.tsx` and `AuthContext.ts` managing `INITIALIZING`, `AUTHENTICATED`, `UNAUTHENTICATED` state transitions and silent session bootstrap on startup without login page flash.
  - Locale-aware React Router (`router.tsx`) supporting PL (`/pl/...`) and EN (`/en/...`) routes with `ProtectedRoute` and `RoleRoute` guards.
  - Multi-language support (`i18next`, `react-i18next`) with Polish and English dictionaries (`pl.json`, `en.json`) and `LanguageSwitcher` component.
  - Light & Dark theme system (`ThemeProvider.tsx`, `useTheme.ts`) with CSS custom properties design tokens, glassmorphic cards, Google Inter/Outfit typography, micro-animations, and `ThemeToggle` component.
  - Reusable UI component library (`Button`, `Input`, `FormField`, `Card`, `Alert`, `Spinner`).
  - User interface screens:
    - `LoginPage.tsx` with Zod validation, rate-limiting (429) & invalid credentials feedback.
    - `DashboardLayout.tsx` with responsive desktop sidebar, mobile navigation drawer, topbar profile summary, language switcher, theme toggle, and logout button.
    - Role-aware landing shells (`AdminDashboardPage`, `OwnerDashboardPage`, `TenantDashboardPage`) and multi-role context switcher (`DashboardDispatcherPage`).
    - `ProfilePage.tsx` displaying user details from `/api/v1/me` and password change form (`POST /api/v1/me/password`).
    - Standardized error pages: `ForbiddenPage` (403), `NotFoundPage` (404), and `GenericErrorPage` with React `ErrorBoundary`.
  - Comprehensive Vitest test suite (`src/test/auth.test.tsx`, `src/test/routing.test.tsx`) covering 11 test scenarios for auth lifecycle, single-flight refresh, routing guards, 403, 404, i18n, and theme toggling.
  - Comprehensive frontend documentation (`docs/development/frontend.md`, `docs/architecture/frontend-foundation.md`, updated `docs/api/authentication.md` and `README.md`).

- **Stage 4 — Authentication and Authorization:**
  - Flyway `V2__create_auth_tables.sql` migration adding `auth_version` and `password_changed_at` to `users`, and creating `refresh_tokens` and `authentication_events` tables.
  - User and role mapping to database tables (`UserEntity`, `RoleEntity`, `RefreshTokenEntity`, `AuthenticationEventEntity`).
  - Password hashing with `BCryptPasswordEncoder(12)` and strict policy validation (`PasswordPolicyValidator`).
  - Safe, idempotent initial admin user bootstrap runner (`AdminBootstrapRunner`) via configurable properties `app.bootstrap-admin.*`.
  - Short-lived HMAC-SHA256 JWT access tokens (`JwtTokenProvider`) with `sub`, `email`, `roles`, `authVersion`, `iss`, `aud` claims.
  - Long-lived 256-bit secure random opaque refresh tokens (`RefreshTokenService`), stored strictly as SHA-256 hashes in DB and sent in `HttpOnly`, `SameSite=Lax` cookies (`prm_refresh_token`).
  - Automatic refresh token rotation with pessimistic locking (`findByTokenHashWithLock`), reuse detection (revoking token family, bumping user `authVersion`), and explicit logout revocation.
  - Double-Submit Cookie CSRF protection (`CookieCsrfTokenRepository`) for cookie-based POST requests (`/api/v1/auth/refresh`, `/api/v1/auth/logout`) with `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header.
  - In-memory Caffeine rate limiter (`LoginRateLimiter`) for login attempts (5 failures / 15 minutes) returning HTTP 429 `RATE_LIMIT_EXCEEDED` with `Retry-After` header.
  - Append-only `authentication_events` audit trail table logging authentication attempts (`LOGIN_SUCCESS`, `LOGIN_FAILURE`, `REFRESH_SUCCESS`, `REFRESH_FAILURE`, `LOGOUT`, `PASSWORD_CHANGED`).
  - Central authorization helper (`PermissionService`) and security principal (`CurrentUser`).
  - Authentication filter (`JwtAuthenticationFilter`), REST authentication entry point (401), and access denied handler (403) rendering standard `ApiErrorResponse`.
  - REST endpoints: `POST /api/v1/auth/login`, `GET /api/v1/auth/csrf`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, `GET /api/v1/me`, `POST /api/v1/me/password`.
  - Comprehensive unit test suite (`JwtTokenProviderTest`, `PasswordPolicyValidatorTest`, `LoginRateLimiterTest`, `PermissionServiceTest`) and PostgreSQL Testcontainers integration test suite (`AuthSecurityIntegrationTest` covering 34 scenarios).
  - Comprehensive security, API, and ERD documentation (`docs/api/authentication.md`, `docs/security/authentication-and-tokens.md`, `docs/architecture/erd.md`).
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
