# Property Rental Manager — Implementation Plan

> **Status:** Active source of truth  
> **Document owner:** Project team  
> **Last updated:** 2026-07-23  
> **Project:** Property Rental Manager  
> **Architecture:** Spring Boot + React + TypeScript + PostgreSQL + Docker Compose  
> **Implementation style:** Modular monolith developed through vertical slices

---

## 1. Purpose of this document

This document is the main source of truth for planning and executing the Property Rental Manager application.

It defines:

- the MVP scope,
- implementation order,
- stage dependencies,
- required deliverables,
- acceptance criteria,
- architectural rules,
- project decisions,
- progress tracking,
- change history.

All implementation work should be mapped to one of the stages described below.

When a decision changes, update this document first and then update the implementation.

---

## 2. Related project documents

This plan should be used together with:

- `Property_Rental_Manager_System_Design(1).docx`
- `property-rental-manager-erd(2).md`

The system design document describes the intended architecture, permissions, workflows, deployment model and API direction.

The ERD document defines the current database model and the relationships between users, roles, properties, tenancies, invoices, files, notifications, settings and audit logs.

---

## 3. Project goal

The application should allow three main roles to use one shared property-management platform:

### Admin

- manages users and roles,
- supervises the platform,
- manages dictionaries and system settings,
- accesses audit logs,
- provides support access where allowed.

### Owner

- creates and manages owned properties,
- assigns tenants,
- creates and publishes invoices,
- uploads invoice files,
- manages invoice statuses,
- reviews historical property and tenancy data.

### Tenant

- views assigned properties,
- views published invoices for the applicable tenancy period,
- downloads authorized files,
- views notifications,
- cannot modify owner-managed data.

---

## 4. MVP scope

The MVP includes:

- secure authentication,
- role-based authorization,
- admin, owner and tenant roles,
- user management,
- property CRUD,
- property ownership relations,
- tenant assignments and tenancy history,
- invoice CRUD,
- invoice publication,
- invoice status workflow,
- PDF and image attachments,
- tenant read-only portal,
- automatic overdue processing,
- in-app notifications,
- audit logging,
- Polish and English translations,
- light and dark theme,
- Docker Compose deployment,
- PostgreSQL database,
- local file storage behind a storage abstraction,
- backup and restore procedures.

The MVP does not include:

- integrated online payments,
- bank reconciliation,
- OCR,
- native mobile applications,
- electronic signatures,
- accounting exports,
- tax calculations,
- advanced contract management,
- multi-organization SaaS tenancy,
- external email notifications unless explicitly added later.

---

## 5. Baseline implementation decisions

These decisions are treated as active until changed in the decision log.

1. The backend is a modular monolith.
2. The backend uses Spring Boot.
3. The frontend uses React and TypeScript.
4. PostgreSQL is the production database.
5. Flyway manages database migrations.
6. Hibernate uses schema validation and does not automatically modify the production schema.
7. The initial deployment uses Docker Compose.
8. File contents are stored outside PostgreSQL.
9. The database stores file metadata and a storage key.
10. Local storage is used first through a `StorageService` abstraction.
11. The frontend supports Polish and English from the beginning.
12. The frontend supports light and dark theme.
13. URLs may use locale prefixes such as `/pl/...` and `/en/...`.
14. A property may support multiple owners in the data model.
15. The first UI may expose one primary owner.
16. A property may have multiple tenants.
17. A tenant may be assigned to multiple properties.
18. Tenancy records are historical and are never replaced by a direct `tenant_id` field on a property.
19. New tenants cannot view invoices from periods before their tenancy.
20. Invoices are not visible to tenants until published.
21. Cancelled invoices remain in the historical record.
22. Tenants cannot directly mark invoices as paid.
23. Owners confirm payment status.
24. Notifications in the MVP are in-app only.
25. Security is enforced on the backend for every protected resource.
26. Frontend role checks are only a UX feature and are never treated as authorization.
27. Sensitive operations are written to append-only audit logs.
28. Business operations should use dedicated endpoints instead of unrestricted generic status updates.

---

## 6. Implementation principles

### 6.1 Vertical slices

Each feature should be implemented across the full stack:

1. database migration,
2. entity and repository,
3. service and business rules,
4. authorization,
5. REST endpoint,
6. frontend API client,
7. screen and interaction,
8. automated tests,
9. documentation update.

Do not build the whole backend first and postpone the frontend until the end.

### 6.2 Definition of Done

A stage is complete only when:

- required code is implemented,
- database migrations are committed,
- backend authorization is enforced,
- validation is implemented,
- tests pass,
- the application starts from a clean environment,
- the feature is usable through the frontend where applicable,
- documentation is updated,
- no known critical issue remains,
- the stage acceptance criteria are verified.

### 6.3 Branching and commits

Recommended workflow:

- `main` — stable branch,
- `develop` — integration branch,
- `feature/<name>` — feature branches,
- `fix/<name>` — bug-fix branches.

Recommended commit format:

```text
feat: add property creation endpoint
fix: prevent tenant access to unpublished invoices
test: add invoice authorization integration tests
docs: update implementation stage status
```

---

## 7. Stage status legend

| Status | Meaning |
|---|---|
| `NOT_STARTED` | Work has not begun |
| `IN_PROGRESS` | Stage is currently being implemented |
| `BLOCKED` | Work is blocked by a decision or technical issue |
| `REVIEW` | Implementation is complete and awaits verification |
| `DONE` | Acceptance criteria have been satisfied |

---

## 8. Master stage tracker

| Stage | Name | Status | Depends on |
|---:|---|---|---|
| 0 | Finalize requirements and decisions | `NOT_STARTED` | — |
| 1 | Repository and development environment | `NOT_STARTED` | 0 |
| 2 | Local infrastructure and database foundation | `NOT_STARTED` | 1 |
| 3 | Backend foundation | `NOT_STARTED` | 2 |
| 4 | Authentication and authorization | `NOT_STARTED` | 3 |
| 5 | Frontend foundation | `NOT_STARTED` | 4 |
| 6 | User administration | `NOT_STARTED` | 4, 5 |
| 7 | Property management | `NOT_STARTED` | 6 |
| 8 | Tenancy management | `NOT_STARTED` | 7 |
| 9 | Invoice lifecycle | `NOT_STARTED` | 8 |
| 10 | File storage and attachments | `NOT_STARTED` | 9 |
| 11 | Tenant portal and visibility rules | `NOT_STARTED` | 9, 10 |
| 12 | Notifications and scheduled automation | `NOT_STARTED` | 9, 11 |
| 13 | Administration, dictionaries and audit | `NOT_STARTED` | 6, 7, 8, 9 |
| 14 | Security, integration and E2E testing | `NOT_STARTED` | 11, 12, 13 |
| 15 | Docker production deployment | `NOT_STARTED` | 14 |
| 16 | MVP stabilization and release | `NOT_STARTED` | 15 |

---

# 9. Detailed implementation stages

## Stage 0 — Finalize requirements and decisions

### Objective

Freeze the MVP rules sufficiently to begin implementation without repeatedly redesigning the domain.

### Tasks

- [ ] Review the current system design document.
- [ ] Review the Mermaid ERD.
- [ ] Confirm role definitions.
- [ ] Confirm invoice visibility rules.
- [ ] Confirm tenancy-history visibility rules.
- [ ] Confirm the invoice publication workflow.
- [ ] Confirm who may change invoice payment statuses.
- [ ] Confirm the initial supported languages.
- [ ] Confirm whether public registration is disabled.
- [ ] Confirm whether owners are created only by admins.
- [ ] Confirm whether tenants are invited by owners or created by admins.
- [ ] Confirm whether multiple owners are exposed in the first UI.
- [ ] Confirm retention rules for files and historical records.
- [ ] Create the decision log.
- [ ] Create the first MVP backlog.

### Deliverables

- `docs/decisions/mvp-decisions.md`
- `docs/requirements/mvp-scope.md`
- approved role-permission matrix,
- approved invoice state diagram,
- approved tenancy visibility rules,
- initial backlog.

### Acceptance criteria

- No unresolved decision blocks authentication, properties, tenancies, invoices or file access.
- All baseline decisions are recorded.
- Any deferred issue is explicitly marked as post-MVP.

---

## Stage 1 — Repository and development environment

### Objective

Create a repeatable project structure and local development workflow.

### Target repository structure

```text
property-rental-manager/
├── backend/
├── frontend/
├── infra/
│   ├── nginx/
│   └── docker/
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── decisions/
│   ├── deployment/
│   └── requirements/
├── scripts/
├── compose.yaml
├── compose.dev.yaml
├── .env.example
├── .editorconfig
├── .gitignore
├── CHANGELOG.md
└── README.md
```

### Tasks

- [ ] Create the repository.
- [ ] Create the directory structure.
- [ ] Configure `.gitignore`.
- [ ] Configure `.editorconfig`.
- [ ] Add `.env.example`.
- [ ] Add README startup instructions.
- [ ] Configure `main`, `develop` and feature branches.
- [ ] Add issue templates.
- [ ] Add pull-request template.
- [ ] Configure basic CI.
- [ ] Verify Java, Maven, Node.js, npm and Docker versions.
- [ ] Configure IntelliJ for the backend.
- [ ] Configure frontend editor tooling.
- [ ] Add formatting and linting rules.

### Deliverables

- initialized repository,
- basic CI,
- documented development setup,
- empty backend and frontend applications that can start.

### Acceptance criteria

- A new developer can clone the repository and start both applications using the README.
- CI runs on a clean commit.
- No secret is committed.

---

## Stage 2 — Local infrastructure and database foundation

### Objective

Provide PostgreSQL, development containers and versioned database migrations.

### Tasks

- [ ] Add PostgreSQL to Docker Compose.
- [ ] Add database health check.
- [ ] Configure persistent development volume.
- [ ] Configure database environment variables.
- [ ] Add optional Adminer or pgAdmin profile.
- [ ] Configure Spring datasource.
- [ ] Enable Flyway.
- [ ] Set Hibernate `ddl-auto` to `validate`.
- [ ] Add the first migration for identity tables.
- [ ] Add database constraints and indexes.
- [ ] Add migration validation tests.
- [ ] Document database reset procedure.

### Planned migrations

```text
V1__create_identity_tables.sql
V2__create_property_tables.sql
V3__create_tenancy_tables.sql
V4__create_invoice_tables.sql
V5__create_file_tables.sql
V6__create_notification_tables.sql
V7__create_audit_tables.sql
```

Migrations may be split further when implementation requires smaller changes.

### Initial identity tables

- `users`
- `roles`
- `user_roles`
- `user_profiles`
- optionally `login_history`
- optionally refresh-token or session tables

### Acceptance criteria

- PostgreSQL starts with Docker Compose.
- A clean database is migrated successfully.
- A second startup does not change the schema unexpectedly.
- Application startup fails when the schema is inconsistent.
- Database reset instructions are verified.

---

## Stage 3 — Backend foundation

### Objective

Create a maintainable Spring Boot foundation before adding domain features.

### Target package structure

```text
pl.propertyrentalmanager
├── auth
├── user
├── property
├── tenancy
├── invoice
├── file
├── notification
├── audit
├── admin
├── security
└── common
```

### Tasks

- [ ] Create Spring Boot project.
- [ ] Configure profiles: development, test, production.
- [ ] Configure global exception handling.
- [ ] Define a stable API error format.
- [ ] Configure DTO validation.
- [ ] Configure Jackson.
- [ ] Add request ID handling.
- [ ] Add structured logging.
- [ ] Add OpenAPI and Swagger.
- [ ] Add health endpoint.
- [ ] Add common pagination response.
- [ ] Add base auditing timestamps.
- [ ] Configure application `Clock`.
- [ ] Add Testcontainers for PostgreSQL integration tests.
- [ ] Add base controller and service test patterns.

### API error format

```json
{
  "code": "PROPERTY_NOT_FOUND",
  "message": "Property was not found",
  "fieldErrors": [],
  "requestId": "f12d8f90..."
}
```

### Acceptance criteria

- Backend starts against PostgreSQL.
- Validation errors use a stable format.
- Exceptions do not expose stack traces in production responses.
- Swagger is available in development.
- Testcontainers integration test passes.

---

## Stage 4 — Authentication and authorization

### Objective

Deliver secure authentication and centralized resource-level authorization.

### Backend scope

- login,
- logout,
- current-user endpoint,
- password change,
- account status checks,
- role resolution,
- token validation,
- login history,
- brute-force protection or rate limiting.

### Planned endpoints

```text
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/me
POST /api/v1/me/password
```

### Central authorization service

```java
boolean canViewProperty(UUID userId, UUID propertyId);
boolean canManageProperty(UUID userId, UUID propertyId);
boolean canViewInvoice(UUID userId, UUID invoiceId);
boolean canManageInvoice(UUID userId, UUID invoiceId);
boolean canDownloadFile(UUID userId, UUID fileId);
```

### Tasks

- [ ] Implement user and role entities.
- [ ] Implement password hashing.
- [ ] Implement JWT access token.
- [ ] Decide and implement refresh-token strategy.
- [ ] Implement authentication filter.
- [ ] Implement `/me`.
- [ ] Implement own-password change.
- [ ] Block disabled users.
- [ ] Record login history.
- [ ] Add login rate limiting.
- [ ] Implement `CurrentUser`.
- [ ] Implement `PermissionService`.
- [ ] Add authentication unit tests.
- [ ] Add endpoint authorization tests.

### Acceptance criteria

- Admin, owner and tenant test accounts can log in.
- Invalid credentials are rejected.
- Disabled users cannot log in.
- `/me` returns roles and effective user information.
- Protected endpoints reject unauthenticated requests.
- Authorization is enforced server-side.

---

## Stage 5 — Frontend foundation

### Objective

Create the reusable frontend shell and connect it to authentication.

### Recommended stack

- React,
- TypeScript,
- Vite,
- React Router,
- TanStack Query,
- React Hook Form,
- Zod,
- i18n library,
- typed API client.

### Target structure

```text
src/
├── api/
├── auth/
├── components/
├── features/
│   ├── users/
│   ├── properties/
│   ├── tenancies/
│   └── invoices/
├── layouts/
├── routes/
├── i18n/
├── themes/
├── types/
└── utils/
```

### Tasks

- [ ] Create Vite React TypeScript project.
- [ ] Configure TypeScript strict mode.
- [ ] Configure linting and formatting.
- [ ] Configure API client.
- [ ] Implement token handling.
- [ ] Implement auth provider.
- [ ] Implement protected routes.
- [ ] Implement role-aware routes.
- [ ] Implement login page.
- [ ] Implement dashboard layout.
- [ ] Implement sidebar and mobile navigation.
- [ ] Implement profile area.
- [ ] Implement own-password change UI.
- [ ] Implement 403, 404 and generic error pages.
- [ ] Add Polish and English.
- [ ] Add light and dark theme.
- [ ] Add loading and empty states.
- [ ] Add frontend tests for authentication.

### Acceptance criteria

- User can log in from the frontend.
- User is redirected according to role.
- Refreshing the browser keeps or safely restores the session.
- Protected routes cannot be opened without authentication.
- Polish and English translations work.
- Light and dark themes work.

---

## Stage 6 — User administration

### Objective

Allow administrators to manage platform users and roles.

### Admin capabilities

- list users,
- search and filter users,
- create owner,
- create tenant,
- create admin,
- change status,
- change roles,
- reset password,
- inspect basic account information.

### User statuses

```text
INVITED
ACTIVE
DISABLED
```

### Planned endpoints

```text
GET   /api/v1/admin/users
POST  /api/v1/admin/users
GET   /api/v1/admin/users/{id}
PATCH /api/v1/admin/users/{id}
POST  /api/v1/admin/users/{id}/reset-password
```

### Tasks

- [ ] Implement user administration service.
- [ ] Add role-change validation.
- [ ] Add account-status transitions.
- [ ] Add password-reset flow.
- [ ] Add audit events for user changes.
- [ ] Create admin users list.
- [ ] Create add/edit user form.
- [ ] Add filters and pagination.
- [ ] Add reset-password modal.
- [ ] Add unit and integration tests.

### Acceptance criteria

- Only admins can use admin user endpoints.
- Admin can create each supported role.
- Disabled users lose login access.
- Role changes are audited.
- Password reset works and follows the selected security policy.

---

## Stage 7 — Property management

### Objective

Allow owners to manage their properties while preserving strict ownership boundaries.

### Property data

```text
friendlyName
description
propertyType
addressLine1
addressLine2
city
postalCode
country
areaSqm
status
```

### Planned endpoints

```text
GET    /api/v1/properties
POST   /api/v1/properties
GET    /api/v1/properties/{id}
PATCH  /api/v1/properties/{id}
DELETE /api/v1/properties/{id}
```

`DELETE` means archive unless an explicit hard-delete policy is introduced later.

### Tasks

- [ ] Add property tables migration.
- [ ] Implement property types.
- [ ] Implement property entity.
- [ ] Implement property-owner relation.
- [ ] Implement property repository queries.
- [ ] Implement property service.
- [ ] Implement ownership authorization.
- [ ] Implement CRUD endpoints.
- [ ] Implement archive and restore.
- [ ] Add property audit events.
- [ ] Create property list UI.
- [ ] Create property form.
- [ ] Create property details page.
- [ ] Add filters and search.
- [ ] Add tests for cross-owner access.

### Acceptance criteria

- Owner sees only owned properties.
- Owner cannot edit another owner's property.
- Admin access follows the approved support policy.
- Tenant access is read-only and only for assigned properties.
- Archived properties are not treated as active.

---

## Stage 8 — Tenancy management

### Objective

Introduce historical tenant-property assignments with date-aware visibility.

### Tenancy data

```text
propertyId
tenantId
startDate
endDate
status
monthlyRent
depositAmount
notes
```

### Statuses

```text
INVITED
ACTIVE
ENDED
CANCELLED
```

### Planned endpoints

```text
GET   /api/v1/properties/{propertyId}/tenancies
POST  /api/v1/properties/{propertyId}/tenancies
PATCH /api/v1/tenancies/{id}
POST  /api/v1/tenancies/{id}/end
```

### Rules

- End date cannot be before start date.
- Historical tenancies are retained.
- Ending a tenancy does not delete invoices.
- An owner can manage tenancies only for owned properties.
- Tenant visibility depends on the active or relevant tenancy period.
- A tenant may have more than one active tenancy.
- A property may have more than one active tenant when allowed.

### Tasks

- [ ] Add tenancy tables migration.
- [ ] Implement tenancy entity and repository.
- [ ] Implement tenancy lifecycle service.
- [ ] Implement overlap policy.
- [ ] Implement date validation.
- [ ] Implement tenant-assignment authorization.
- [ ] Implement endpoints.
- [ ] Add audit events.
- [ ] Add active and historical tenant UI.
- [ ] Add assign-tenant form.
- [ ] Add end-tenancy action.
- [ ] Add visibility query tests.

### Acceptance criteria

- Owner can assign a tenant to an owned property.
- Tenant can see the assigned property.
- Ended tenancy remains in history.
- Unauthorized users cannot inspect or modify tenancy records.
- Date rules are enforced by backend validation.

---

## Stage 9 — Invoice lifecycle

### Objective

Implement the central invoice workflow.

### Invoice statuses

```text
DRAFT
PENDING
PAID
OVERDUE
CANCELLED
```

### Allowed transitions

```text
DRAFT -> PENDING
DRAFT -> CANCELLED
PENDING -> PAID
PENDING -> OVERDUE
PENDING -> CANCELLED
OVERDUE -> PAID
OVERDUE -> CANCELLED
```

Any additional transition must be added explicitly to the transition policy.

### Invoice data

```text
propertyId
tenancyId
invoiceType
invoiceNumber
vendor
issueDate
paymentDueDate
billingPeriodFrom
billingPeriodTo
amount
currency
description
status
isPublished
publishedAt
```

### Planned endpoints

```text
GET   /api/v1/properties/{propertyId}/invoices
POST  /api/v1/properties/{propertyId}/invoices
GET   /api/v1/invoices/{id}
PATCH /api/v1/invoices/{id}
POST  /api/v1/invoices/{id}/publish
POST  /api/v1/invoices/{id}/unpublish
POST  /api/v1/invoices/{id}/mark-paid
POST  /api/v1/invoices/{id}/cancel
```

### Tasks

- [ ] Add invoice migrations.
- [ ] Implement invoice types.
- [ ] Implement invoice entity.
- [ ] Implement invoice repositories.
- [ ] Implement `InvoiceStatusTransitionService`.
- [ ] Implement publish and unpublish rules.
- [ ] Implement billing-period validation.
- [ ] Implement amount and currency validation.
- [ ] Implement property and tenancy scope checks.
- [ ] Implement invoice endpoints.
- [ ] Add audit events.
- [ ] Create invoice list UI.
- [ ] Create invoice form.
- [ ] Create invoice details UI.
- [ ] Add status badges and filters.
- [ ] Add status-transition tests.

### Acceptance criteria

- Owner can create an invoice for an owned property.
- Invalid status transitions are rejected.
- Invoice publication is explicit.
- Unpublished invoices are hidden from tenants.
- Invoice changes are audited.
- Cross-owner invoice access is rejected.

---

## Stage 10 — File storage and attachments

### Objective

Allow secure file upload and download without exposing storage paths.

### Storage abstraction

```java
public interface StorageService {
    StoredFile store(InputStream input, FileMetadata metadata);
    InputStream load(String storageKey);
    void delete(String storageKey);
}
```

### Initial implementation

```text
LocalStorageService
```

### Future implementations

```text
MinioStorageService
S3StorageService
```

### Accepted types

- `application/pdf`
- `image/jpeg`
- `image/png`
- `image/webp`

### Planned endpoints

```text
POST   /api/v1/invoices/{invoiceId}/files
GET    /api/v1/files/{fileId}/download
DELETE /api/v1/files/{fileId}
```

### Tasks

- [ ] Add file and invoice-file migrations.
- [ ] Implement file metadata entity.
- [ ] Implement `StorageService`.
- [ ] Implement local storage driver.
- [ ] Generate safe storage keys.
- [ ] Calculate SHA-256 checksums.
- [ ] Validate MIME type.
- [ ] Validate extension.
- [ ] Validate file size.
- [ ] Prevent path traversal.
- [ ] Implement authorized download.
- [ ] Implement deletion or soft-deletion policy.
- [ ] Add file audit events.
- [ ] Add file uploader UI.
- [ ] Add preview/download UI.
- [ ] Add malicious-input and authorization tests.

### Acceptance criteria

- User-provided filename is never used as a storage path.
- Tenant can download only authorized files.
- Unsupported files are rejected.
- Oversized files are rejected.
- Deleted files follow the approved retention policy.
- Raw storage paths are never exposed.

---

## Stage 11 — Tenant portal and visibility rules

### Objective

Deliver the complete read-only tenant experience.

### Tenant can

- view active assigned properties,
- view applicable tenancy information,
- view published invoices,
- view invoice statuses,
- preview or download authorized files,
- view notifications,
- manage own profile and password.

### Tenant cannot

- create invoices,
- edit invoices,
- publish invoices,
- upload files,
- delete files,
- change invoice statuses,
- view other tenants' resources,
- view invoices from before the tenancy period,
- view unpublished invoices.

### Security scenarios

```text
Tenant A requests Invoice B assigned to Tenant B -> denied
Tenant changes invoice ID in URL -> denied
Tenant requests a hidden file -> denied
Tenant requests an unpublished invoice -> denied
New tenant requests a pre-tenancy invoice -> denied
```

### Tasks

- [ ] Implement tenant dashboard queries.
- [ ] Implement date-aware invoice visibility.
- [ ] Decide whether denied resources return 403 or 404.
- [ ] Implement assigned-property page.
- [ ] Implement tenant invoice list.
- [ ] Implement read-only invoice viewer.
- [ ] Implement secure file download.
- [ ] Add tenant navigation.
- [ ] Add mobile-responsive layout.
- [ ] Add tenant authorization integration tests.
- [ ] Add E2E visibility tests.

### Acceptance criteria

- Tenant can complete the intended read-only workflow.
- All cross-tenant access tests fail safely.
- Historical invoice rules work.
- Tenant UI contains no mutating actions.
- Backend rejects mutating calls regardless of frontend state.

---

## Stage 12 — Notifications and scheduled automation

### Objective

Automate overdue processing and notify relevant users inside the application.

### Overdue rule

```sql
status = 'PENDING'
AND payment_due_date < CURRENT_DATE
```

The scheduler changes the status to:

```text
OVERDUE
```

### Notification types

```text
INVOICE_PUBLISHED
INVOICE_DUE_SOON
INVOICE_OVERDUE
INVOICE_PAID
TENANCY_ASSIGNED
```

### Planned endpoints

```text
GET  /api/v1/notifications
POST /api/v1/notifications/{id}/read
POST /api/v1/notifications/read-all
```

### Tasks

- [ ] Add notification migration.
- [ ] Implement notification entity and service.
- [ ] Implement configurable scheduler.
- [ ] Use application `Clock`.
- [ ] Make overdue processing idempotent.
- [ ] Add audit entries for automated transitions.
- [ ] Add notification creation rules.
- [ ] Add notification list UI.
- [ ] Add unread count.
- [ ] Add mark-as-read actions.
- [ ] Add scheduler integration tests.
- [ ] Add duplicate-processing tests.

### Acceptance criteria

- Running the overdue job twice does not corrupt data.
- Only pending invoices become overdue.
- Paid and cancelled invoices are not changed.
- Relevant users receive in-app notifications.
- Automated changes are audited.

---

## Stage 13 — Administration, dictionaries and audit

### Objective

Complete platform-level administration and operational visibility.

### Administrative areas

- property types,
- invoice types,
- supported currencies,
- supported locales,
- system settings,
- audit logs.

### Audit events

```text
user.created
user.role_changed
property.created
property.updated
property.archived
tenancy.assigned
tenancy.ended
invoice.created
invoice.published
invoice.status_changed
invoice.file_uploaded
invoice.file_deleted
```

### Tasks

- [ ] Add system-settings migration.
- [ ] Add audit-log migration.
- [ ] Implement dictionary services.
- [ ] Separate code-controlled enums from configurable dictionaries.
- [ ] Implement append-only audit service.
- [ ] Include request ID in audit records.
- [ ] Include actor information.
- [ ] Add admin dictionary UI.
- [ ] Add system settings UI.
- [ ] Add audit log list.
- [ ] Add filters by date, actor, entity and action.
- [ ] Add authorization tests.
- [ ] Prevent audit modification through normal endpoints.

### Acceptance criteria

- Admin can manage approved dictionaries.
- Code-controlled statuses cannot be broken through configuration.
- Audit logs are read-only.
- Important business operations are traceable.
- Non-admin users cannot access global administration.

---

## Stage 14 — Security, integration and E2E testing

### Objective

Verify the full application as a coherent and secure system.

### Backend unit tests

- invoice status transitions,
- permission service,
- tenancy date rules,
- visibility rules,
- file validation,
- scheduler idempotency.

### Integration tests

- Flyway migrations,
- PostgreSQL constraints,
- REST endpoints,
- file upload and download,
- authorization,
- transactions,
- audit records.

### Frontend tests

- route protection,
- forms,
- validation,
- translation rendering,
- role-specific navigation,
- error states,
- status badges.

### E2E scenario

1. Admin creates an owner.
2. Admin creates a tenant.
3. Owner logs in.
4. Owner creates a property.
5. Owner assigns the tenant.
6. Owner creates an invoice.
7. Owner uploads a PDF.
8. Tenant cannot see the invoice before publication.
9. Owner publishes the invoice.
10. Tenant sees and downloads the invoice.
11. Tenant cannot edit the invoice.
12. Scheduler marks the invoice overdue after the due date.
13. Owner marks the invoice paid.
14. Audit log contains the relevant events.

### Security tests

- IDOR attempts,
- cross-owner access,
- cross-tenant access,
- unpublished resource access,
- path traversal,
- invalid MIME types,
- oversized files,
- expired tokens,
- disabled users,
- missing backend permission checks.

### Acceptance criteria

- Critical E2E flow passes.
- Security tests pass.
- No high-severity authorization issue remains.
- Database migrations work from an empty database.
- Test suite runs in CI.

---

## Stage 15 — Docker production deployment

### Objective

Create a repeatable and secure production deployment.

### Target architecture

```text
Internet
   |
Reverse Proxy / HTTPS
   |
   +-- Frontend
   +-- Backend
           |
           +-- PostgreSQL
           +-- Storage volume
```

### Environments

```text
development
test
staging
production
```

### Tasks

- [ ] Add backend multi-stage Dockerfile.
- [ ] Add frontend multi-stage Dockerfile.
- [ ] Add production Compose file.
- [ ] Add reverse-proxy configuration.
- [ ] Configure HTTPS.
- [ ] Configure CORS.
- [ ] Configure secure secrets.
- [ ] Add backend health check.
- [ ] Add frontend health check.
- [ ] Add database health check.
- [ ] Add restart policies.
- [ ] Add upload limits.
- [ ] Add log rotation.
- [ ] Add database backup script.
- [ ] Add storage backup script.
- [ ] Add restore script.
- [ ] Perform backup-restore drill.
- [ ] Document deployment.

### Acceptance criteria

- Production environment starts from documented commands.
- Application is reachable over HTTPS.
- Secrets are not stored in the repository.
- Containers recover after a host restart.
- Backup and restore are tested.
- A clean installation is reproducible.

---

## Stage 16 — MVP stabilization and release

### Objective

Prepare the first stable release for real use.

### Tasks

- [ ] Perform complete permission review.
- [ ] Test URL and ID manipulation.
- [ ] Test unsupported files.
- [ ] Test large files.
- [ ] Test date boundaries and time zones.
- [ ] Test Polish and English.
- [ ] Test light and dark theme.
- [ ] Test mobile layout.
- [ ] Test a clean Docker installation.
- [ ] Test backup restoration.
- [ ] Remove test secrets.
- [ ] Review logs for sensitive data.
- [ ] Review dependency vulnerabilities.
- [ ] Prepare release notes.
- [ ] Create admin manual.
- [ ] Create owner manual.
- [ ] Create tenant manual.
- [ ] Tag the MVP release.

### Required documentation

```text
README.md
CHANGELOG.md
docs/deployment.md
docs/backup-and-restore.md
docs/admin-manual.md
docs/owner-manual.md
docs/tenant-manual.md
```

### Acceptance criteria

- No critical defect remains.
- MVP acceptance scenario passes.
- Installation and recovery documentation is verified.
- Release is tagged.
- The application is ready for controlled production use.

---

# 10. Milestones

## Milestone 1 — Project Foundation

Includes:

- Stage 0,
- Stage 1,
- Stage 2,
- Stage 3.

Result:

- project structure,
- local environment,
- PostgreSQL,
- Flyway,
- backend foundation.

## Milestone 2 — Identity and Access

Includes:

- Stage 4,
- Stage 5,
- Stage 6.

Result:

- authentication,
- role-based application shell,
- user administration.

## Milestone 3 — Property and Tenancy

Includes:

- Stage 7,
- Stage 8.

Result:

- property ownership,
- tenant assignment,
- tenancy history.

## Milestone 4 — Invoice Management

Includes:

- Stage 9,
- Stage 10.

Result:

- invoice workflow,
- publication,
- attachments,
- secure file storage.

## Milestone 5 — Tenant Portal and Automation

Includes:

- Stage 11,
- Stage 12,
- Stage 13.

Result:

- tenant portal,
- visibility enforcement,
- notifications,
- scheduler,
- administration,
- audit.

## Milestone 6 — Production MVP

Includes:

- Stage 14,
- Stage 15,
- Stage 16.

Result:

- tested,
- secured,
- documented,
- deployable MVP.

---

# 11. Recommended implementation order by vertical slice

```text
1. Login
2. Current user and roles
3. Admin creates owner
4. Owner creates property
5. Admin creates tenant
6. Owner assigns tenant
7. Owner creates invoice
8. Owner uploads file
9. Owner publishes invoice
10. Tenant views invoice
11. Scheduler marks invoice overdue
12. Owner confirms payment
13. Audit and notifications
14. Docker production deployment
```

After every slice:

- application must start,
- migrations must work,
- tests must pass,
- documentation must remain current.

---

# 12. First sprint

The first sprint should deliver:

- PostgreSQL in Docker,
- Flyway,
- identity tables,
- Spring Security,
- JWT login,
- `/api/v1/me`,
- login screen,
- authenticated application layout,
- role-based redirect,
- Polish and English language foundation,
- light and dark theme foundation,
- authentication tests.

The team should not start property management before this sprint is complete.

---

# 13. Change-control rules

1. A stage may not be marked `DONE` until its acceptance criteria are met.
2. New scope should be added to the backlog before implementation.
3. A business-rule change must be recorded in the decision log.
4. A database-model change must be reflected in:
   - Flyway migrations,
   - ERD,
   - related API documentation,
   - this implementation plan when it changes sequencing or scope.
5. Any deferred issue must have:
   - an owner,
   - a reason,
   - a target stage or post-MVP marker.
6. Security exceptions must never remain undocumented.
7. Implementation status should be updated at the end of every working session.

---

# 14. Stage execution template

Use this template when starting each stage.

```markdown
## Stage execution record

- Stage:
- Status:
- Start date:
- Completion date:
- Branch:
- Main objective:
- Decisions required:
- Blocking issues:
- Implemented items:
- Tests added:
- Documentation updated:
- Remaining issues:
- Acceptance criteria result:
```

---

# 15. Decision log

| ID | Date | Decision | Status | Notes |
|---|---|---|---|---|
| D-001 | 2026-07-23 | Use a modular monolith | Accepted | Spring Boot backend divided into domain modules |
| D-002 | 2026-07-23 | Use React + TypeScript frontend | Accepted | Vite-based frontend |
| D-003 | 2026-07-23 | Use PostgreSQL and Flyway | Accepted | Hibernate schema validation only |
| D-004 | 2026-07-23 | Use Docker Compose for initial deployment | Accepted | Cloud migration remains possible |
| D-005 | 2026-07-23 | Use local file storage behind `StorageService` for MVP | Accepted | MinIO/S3 may be added later |
| D-006 | 2026-07-23 | Tenants see only published and tenancy-applicable invoices | Accepted | Historical visibility is date-aware |
| D-007 | 2026-07-23 | Notifications are in-app only for MVP | Accepted | Email notifications deferred |
| D-008 | 2026-07-23 | Support Polish and English from the beginning | Accepted | Additional locales may be added later |
| D-009 | 2026-07-23 | Support light and dark theme | Accepted | Theme preference may be persisted later |

---

# 16. Progress log

| Date | Stage | Change | Result |
|---|---:|---|---|
| 2026-07-23 | Planning | Created implementation source-of-truth document | Ready to begin Stage 0 |

---

# 17. Current next action

The next action is:

> **Begin Stage 0 — Finalize requirements and decisions.**

Stage 0 should end with an approved `mvp-decisions.md` file and a confirmed backlog for Stage 1.
