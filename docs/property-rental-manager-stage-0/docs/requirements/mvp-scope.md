# Property Rental Manager — MVP Scope

> **Document status:** Proposed baseline for approval  
> **Stage:** 0 — Finalize requirements and decisions  
> **Last updated:** 2026-07-23

## 1. Product objective

Property Rental Manager provides one shared platform for administrators, property owners and tenants. The MVP must support the complete workflow from account creation, through property and tenancy management, to publication and secure tenant access to utility invoices and their attachments.

## 2. MVP actors

- **Admin** — manages users, roles, dictionaries, system settings and audit data; performs audited support actions.
- **Owner** — manages owned properties, tenancies, invoices, publication and payment status.
- **Tenant** — reads assigned property data, applicable published invoices, files and notifications.

## 3. In-scope capabilities

### 3.1 Identity and access

- email and password authentication,
- secure password hashing,
- JWT-based authenticated API access,
- logout and current-user endpoint,
- own-password change,
- admin password reset,
- account states: invited, active and disabled,
- roles: admin, owner and tenant,
- backend resource-level authorization,
- login history and basic rate limiting.

### 3.2 User administration

- admin creates admin, owner and tenant accounts,
- admin changes account status and roles,
- admin resets passwords,
- public registration disabled,
- owner may invite a tenant for an owned property,
- invitation/activation message may be delivered by email.

### 3.3 Property management

- owner creates, edits, archives and restores an owned property,
- property has a structured address, friendly name, description, type, area and status,
- property creation automatically creates the primary owner relation,
- data model supports multiple owners,
- initial UI exposes one primary owner,
- tenant access is read-only.

### 3.4 Tenancy management

- owner assigns or invites a tenant to an owned property,
- tenancy stores start date, optional end date, status and optional financial metadata,
- tenancy history is retained,
- one property may have multiple tenants,
- one tenant may have multiple properties,
- former tenants retain access only to invoices applicable to their tenancy period.

### 3.5 Invoice management

- invoice types and stable statuses,
- invoice creation in `DRAFT`,
- explicit publish and unpublish operations,
- payment due date, issue date, billing period, amount, currency, vendor and notes,
- published `PENDING` invoices visible to applicable tenants,
- owner/admin can mark pending or overdue invoices paid,
- automatic overdue processing,
- cancellation with mandatory reason,
- cancelled invoices retained in history,
- invoice activity recorded in audit logs.

### 3.6 File handling

- PDF, JPEG, PNG and WebP attachments,
- file metadata stored in PostgreSQL,
- file content stored through `StorageService`,
- generated storage keys,
- MIME type, extension and size validation,
- SHA-256 checksum,
- authorized download through backend,
- no exposure of raw storage paths,
- owner may remove files before publication,
- published files are retained except audited admin emergency removal.

### 3.7 Tenant portal

- list assigned properties,
- view applicable tenancy data,
- list published and applicable invoices,
- view invoice status and metadata,
- preview or download authorized files,
- view and mark in-app notifications as read,
- responsive read-only UI,
- profile and password management.

### 3.8 Administration and audit

- admin user management,
- property types and invoice types,
- supported currencies and locales,
- system settings,
- append-only audit logs,
- filters by actor, entity, action and date,
- request ID included in logs and audit events.

### 3.9 User experience

- Polish and English translations,
- Polish default locale,
- light and dark themes,
- role-aware navigation,
- desktop and mobile-responsive layouts,
- consistent loading, empty, validation and error states,
- accessibility-oriented semantic HTML and keyboard navigation.

### 3.10 Deployment and operations

- Docker Compose deployment,
- Spring Boot backend,
- React static frontend,
- PostgreSQL database,
- reverse proxy with HTTPS,
- persistent storage volume,
- environment-driven configuration,
- health checks,
- database and file backup/restore procedures.

## 4. Out of scope for MVP

- public self-registration,
- bank integrations and automatic reconciliation,
- tenant-side paid confirmation or review request,
- integrated online payments,
- OCR and invoice field extraction,
- native mobile applications,
- electronic signatures,
- accounting and tax exports,
- advanced rental contract management,
- maintenance request module,
- multi-organization SaaS tenancy,
- multi-owner management UI,
- operational email/SMS invoice reminders,
- user impersonation,
- automated legal retention and purge policies.

## 5. Core business invariants

1. Backend authorization is required for every protected operation.
2. An owner manages a property only through a valid ownership relation.
3. A tenant never receives access merely because the tenant knows a resource ID.
4. Tenant invoice access requires publication and tenancy applicability.
5. A new tenant cannot view pre-tenancy invoices.
6. A former tenant may view invoices applicable to their own historical tenancy.
7. `DRAFT` invoices are never tenant-visible.
8. Publication changes `DRAFT` to `PENDING`.
9. Unpublication changes `PENDING` to `DRAFT` and is not allowed from final/financial states.
10. The overdue scheduler changes only published `PENDING` invoices whose due date has passed.
11. Paid and cancelled invoices are not changed by the overdue scheduler.
12. Published business records are retained; lifecycle operations replace destructive deletion.
13. Audit logs are append-only.
14. Storage keys are generated by the backend and raw paths are never returned.

## 6. MVP acceptance scenario

1. A bootstrap admin logs in.
2. Admin creates an owner account.
3. Admin creates a tenant account or owner sends a property-related invitation.
4. Owner logs in and creates a property.
5. Owner assigns the tenant with a tenancy start date.
6. Owner creates a draft invoice and uploads a PDF.
7. Tenant cannot see the draft invoice.
8. Owner publishes the invoice.
9. Tenant sees the invoice because its billing period is applicable to the tenancy.
10. Tenant downloads the authorized file.
11. Tenant cannot edit, delete, publish or mark the invoice paid.
12. The scheduler marks the invoice overdue after the due date.
13. Owner marks the overdue invoice paid.
14. Audit logs contain account, property, tenancy, invoice, file and status events.
15. A second tenant assigned later cannot access the earlier invoice.

## 7. Non-functional acceptance boundary

- no known high-severity IDOR or cross-tenant authorization issue,
- database migrations succeed from an empty database,
- application starts through documented Docker commands,
- secrets are not committed,
- file validation rejects unsupported and oversized uploads,
- core tests run in CI,
- Polish and English interfaces are usable,
- light and dark themes are usable,
- backup and restore procedure is verified before release.
