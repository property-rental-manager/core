# Property Rental Manager — Initial MVP Backlog

> **Document status:** Initial backlog prepared in Stage 0  
> **Priorities:** `P0` required for MVP; `P1` important but may follow the first vertical slice; `P2` post-MVP.

## 1. Backlog conventions

Each story must include database, backend, authorization, frontend, tests and documentation work when applicable. A story is complete only when its acceptance criteria pass in a clean environment.

## 2. Epics and stories

### EPIC E-01 — Repository and developer experience

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-001 | P0 | Create repository structure and baseline documentation. | 1 | Backend, frontend, infra, docs and scripts directories exist. |
| PRM-002 | P0 | Configure Git ignore, editor settings and environment examples. | 1 | No secret or generated build output is tracked. |
| PRM-003 | P0 | Add CI for backend and frontend validation. | 1 | Clean commit runs build/tests/lint. |
| PRM-004 | P0 | Document local setup for Java, Maven, Node and Docker. | 1 | New developer can start both applications. |

### EPIC E-02 — Database and backend foundation

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-010 | P0 | Start PostgreSQL with Docker Compose and health check. | 2 | Database becomes healthy and persistent. |
| PRM-011 | P0 | Configure Flyway and schema validation. | 2 | Empty DB migrates; inconsistent schema blocks startup. |
| PRM-012 | P0 | Create identity migrations. | 2 | Users, roles and user-role relations exist with constraints. |
| PRM-013 | P0 | Add stable API error model and validation handling. | 3 | Validation and domain errors use documented format. |
| PRM-014 | P0 | Add request IDs, logging and health endpoint. | 3 | Request ID appears in logs and responses where required. |
| PRM-015 | P0 | Configure Testcontainers integration test baseline. | 3 | PostgreSQL-backed integration test passes in CI. |

### EPIC E-03 — Authentication and frontend shell

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-020 | P0 | Implement password hashing and bootstrap admin. | 4 | Admin can be created without committed credentials. |
| PRM-021 | P0 | Implement login and JWT validation. | 4 | Valid credentials authenticate; invalid/disabled accounts fail. |
| PRM-022 | P0 | Implement current-user endpoint. | 4 | `/me` returns identity and roles. |
| PRM-023 | P0 | Implement own-password change. | 4, 5 | Authenticated user can change password after validation. |
| PRM-024 | P0 | Implement centralized `PermissionService`. | 4 | Resource-level authorization methods are covered by tests. |
| PRM-025 | P0 | Create React shell, API client and auth provider. | 5 | Login creates authenticated application session. |
| PRM-026 | P0 | Add protected and role-aware routes. | 5 | Unauthorized navigation fails safely. |
| PRM-027 | P0 | Add Polish/English and light/dark theme foundations. | 5 | Both locales and themes work in login and dashboard shell. |

### EPIC E-04 — User administration and invitations

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-030 | P0 | Admin lists, filters and views users. | 6 | Admin-only list with pagination/search. |
| PRM-031 | P0 | Admin creates admin, owner and tenant accounts. | 6 | Role and status validation enforced. |
| PRM-032 | P0 | Admin changes role and account status. | 6 | Disabled users cannot authenticate; changes audited. |
| PRM-033 | P0 | Admin resets another user’s password. | 6 | Reset policy works and is audited. |
| PRM-034 | P1 | Owner invites tenant for an owned property. | 8 | Invitation cannot target non-owned property. |
| PRM-035 | P1 | Tenant accepts invitation and activates account. | 8 | Invitation creates/activates account and tenancy atomically. |

### EPIC E-05 — Property management

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-040 | P0 | Create property and primary ownership relation. | 7 | Property and owner relation commit in one transaction. |
| PRM-041 | P0 | List properties according to role visibility. | 7 | Admin all, owner owned, tenant assigned. |
| PRM-042 | P0 | Edit owned property. | 7 | Cross-owner edit is rejected. |
| PRM-043 | P0 | Archive and restore property. | 7 | Archived property is excluded from active workflows. |
| PRM-044 | P0 | Build property list, form and details pages. | 7 | Role-appropriate actions and validation work. |

### EPIC E-06 — Tenancy management

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-050 | P0 | Assign existing tenant to owned property. | 8 | Date rules and ownership checks pass. |
| PRM-051 | P0 | List active and historical tenancies. | 8 | History is retained and separated in UI. |
| PRM-052 | P0 | End or cancel tenancy. | 8 | No destructive deletion; audit event created. |
| PRM-053 | P0 | Implement overlap and visibility query helpers. | 8, 11 | Boundary tests pass. |
| PRM-054 | P0 | Build tenant assignment and history UI. | 8 | Owner can manage tenancy lifecycle. |

### EPIC E-07 — Invoice lifecycle

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-060 | P0 | Create draft invoice for owned property. | 9 | Tenant cannot see the draft. |
| PRM-061 | P0 | Edit draft invoice with date/amount validation. | 9 | Invalid billing periods and amounts are rejected. |
| PRM-062 | P0 | Publish draft invoice. | 9 | Status becomes pending; tenant visibility starts when applicable. |
| PRM-063 | P0 | Unpublish pending invoice. | 9 | Status returns to draft; tenant visibility ends. |
| PRM-064 | P0 | Mark pending/overdue invoice paid. | 9 | Tenant cannot perform operation; event audited. |
| PRM-065 | P0 | Cancel invoice with reason. | 9 | Historical visibility follows publication history. |
| PRM-066 | P0 | Build invoice list, form and details pages. | 9 | Filters, badges and operation guards work. |

### EPIC E-08 — Secure file storage

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-070 | P0 | Implement `StorageService` and local driver. | 10 | Files use generated keys outside DB. |
| PRM-071 | P0 | Validate PDF/image MIME, extension and size. | 10 | Unsupported and oversized files fail safely. |
| PRM-072 | P0 | Calculate checksum and store metadata. | 10 | SHA-256 saved with file record. |
| PRM-073 | P0 | Implement authorized download. | 10 | Raw paths are hidden; cross-tenant download denied. |
| PRM-074 | P0 | Enforce state-aware attachment deletion. | 10 | Owner removal blocked after publication. |
| PRM-075 | P0 | Build upload, preview and download UI. | 10 | Owner uploads; tenant read-only download works. |

### EPIC E-09 — Tenant portal

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-080 | P0 | Build tenant dashboard and assigned property views. | 11 | Active and historical assignments display correctly. |
| PRM-081 | P0 | Implement date-aware invoice list. | 11 | New tenant cannot see earlier invoices. |
| PRM-082 | P0 | Build read-only invoice viewer. | 11 | No mutating controls; backend also rejects mutations. |
| PRM-083 | P0 | Implement secure tenant file access. | 11 | Applicable published files only. |
| PRM-084 | P0 | Add cross-tenant and IDOR integration tests. | 11, 14 | All unauthorized scenarios fail safely. |

### EPIC E-10 — Automation, notifications and audit

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-090 | P0 | Implement idempotent overdue scheduler. | 12 | Only published pending overdue invoices transition. |
| PRM-091 | P0 | Create in-app notifications and unread count. | 12 | Relevant users receive and read notifications. |
| PRM-092 | P0 | Implement append-only audit service. | 13 | Business events include actor and request ID. |
| PRM-093 | P0 | Build admin audit log UI. | 13 | Admin filters by date, actor, entity and action. |
| PRM-094 | P1 | Build configurable dictionaries/settings UI. | 13 | Admin manages approved values without breaking code enums. |

### EPIC E-11 — Verification and release

| ID | Priority | Story | Stage | Acceptance summary |
|---|---:|---|---:|---|
| PRM-100 | P0 | Run complete MVP E2E scenario. | 14 | End-to-end acceptance flow passes. |
| PRM-101 | P0 | Run security tests for IDOR, files and tokens. | 14 | No high-severity authorization issue remains. |
| PRM-102 | P0 | Create production Docker images and Compose stack. | 15 | Clean production start works. |
| PRM-103 | P0 | Configure HTTPS, CORS, health checks and restart policies. | 15 | Production routing and recovery work. |
| PRM-104 | P0 | Implement and test database/file backup restore. | 15 | Restore drill succeeds. |
| PRM-105 | P0 | Prepare release manuals and tag MVP. | 16 | Controlled production release is documented. |

## 3. First implementation slice

The first implementation slice after Stage 0 is:

```text
PostgreSQL + Flyway
→ identity schema
→ bootstrap admin
→ login API
→ /api/v1/me
→ React login
→ authenticated layout
→ role redirect
→ PL/EN
→ light/dark theme
→ authentication tests
```

## 4. Post-MVP backlog

| ID | Priority | Item |
|---|---:|---|
| PRM-200 | P2 | Multi-owner management UI and ownership shares. |
| PRM-201 | P2 | Tenant payment-report/review workflow. |
| PRM-202 | P2 | Email reminders for new, due and overdue invoices. |
| PRM-203 | P2 | OCR invoice extraction. |
| PRM-204 | P2 | Online payments and bank reconciliation. |
| PRM-205 | P2 | Rental contracts and electronic signatures. |
| PRM-206 | P2 | Maintenance requests and inspections. |
| PRM-207 | P2 | Multi-organization SaaS tenancy. |
