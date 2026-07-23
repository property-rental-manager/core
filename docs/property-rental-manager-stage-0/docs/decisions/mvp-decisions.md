# Property Rental Manager — MVP Decision Log

> **Document status:** Proposed baseline for approval  
> **Stage:** 0 — Finalize requirements and decisions  
> **Last updated:** 2026-07-23

## 1. Purpose

This document records binding business and architectural decisions for the MVP. When implementation behavior conflicts with this document, either the implementation must be corrected or the decision must be formally changed and recorded here first.

## 2. Accepted baseline decisions

| ID | Decision | Status | Implementation consequence |
|---|---|---|---|
| D-001 | The backend is a modular monolith implemented in Spring Boot. | Accepted | Domain modules remain inside one deployable backend application. |
| D-002 | The frontend uses React and TypeScript with Vite. | Accepted | Frontend code is strongly typed and separated into feature modules. |
| D-003 | PostgreSQL is the relational database and Flyway owns schema changes. | Accepted | Hibernate uses schema validation and must not modify the production schema. |
| D-004 | Initial deployment uses Docker Compose. | Accepted | Production packaging must remain portable to a future managed environment. |
| D-005 | File content is stored outside PostgreSQL behind `StorageService`. | Accepted | The database stores metadata, checksum and storage key only. |
| D-006 | Authorization is enforced by the backend for every protected resource. | Accepted | Frontend role checks are UX only. Resource-level checks are mandatory. |
| D-007 | Sensitive business operations are recorded in append-only audit logs. | Accepted | Normal application endpoints cannot edit or delete audit entries. |
| D-008 | The MVP supports Polish and English. | Accepted | Translation keys are required from the first frontend feature. |
| D-009 | The MVP supports light and dark themes. | Accepted | Theme support is part of the frontend foundation. |
| D-010 | The three application roles are `ADMIN`, `OWNER` and `TENANT`. | Accepted | Roles are code-controlled and cannot be renamed through dictionaries. |
| D-011 | Admins may view and support-manage all platform resources. Every admin write to owner or tenant data is audited. | Accepted | No hidden “superuser” bypass exists outside the standard authorization layer. |
| D-012 | User impersonation is not included in the MVP. | Accepted | Support actions are performed as the admin and attributed to the admin. |
| D-013 | Public self-registration is disabled. | Accepted | Accounts originate from admin creation or a tenant invitation. |
| D-014 | Admins create admin and owner accounts. Admins may also create tenant accounts. | Accepted | Owner onboarding is controlled centrally. |
| D-015 | Owners may invite tenants only in relation to a property they own. | Accepted | Invitation acceptance creates or activates the tenant account and the tenancy assignment. |
| D-016 | Invitation email is permitted as a transactional onboarding message. Other operational email notifications are deferred. | Accepted | MVP notifications remain in-app, except account invitation/activation delivery. |
| D-017 | A property supports multiple owners in the data model, but the first UI exposes one primary owner. | Accepted | Co-owner editing and ownership shares are post-MVP UI work. |
| D-018 | When an owner creates a property, that owner becomes its primary owner automatically. | Accepted | `property_owners` is written in the same transaction as the property. |
| D-019 | A property may have multiple tenants and a tenant may be assigned to multiple properties. | Accepted | Tenancy is a historical relation, never a direct `tenant_id` column on property. |
| D-020 | Former tenants retain access to published invoices applicable to their own tenancy period. | Accepted | Access is date-aware and does not require the tenancy to remain active. |
| D-021 | New tenants cannot view invoices applicable only to periods before their tenancy. | Accepted | Property assignment alone is insufficient for invoice visibility. |
| D-022 | Invoice visibility is controlled by both publication state and tenancy applicability. | Accepted | A tenant must satisfy both conditions. |
| D-023 | Invoice publication is explicit. `DRAFT` is unpublished; publishing changes `DRAFT` to `PENDING`. | Accepted | Tenant visibility never starts automatically on invoice creation. |
| D-024 | Unpublishing is allowed only for a `PENDING` invoice and changes it back to `DRAFT`. | Accepted | `PAID`, `OVERDUE` and `CANCELLED` invoices cannot be unpublished. |
| D-025 | Tenants cannot mark an invoice as paid and cannot request a payment-status review in the MVP. | Accepted | Owner or admin performs payment status changes. |
| D-026 | Owners may mark `PENDING` or `OVERDUE` invoices as `PAID`. | Accepted | Payment confirmation is a dedicated operation, not a generic status patch. |
| D-027 | Cancelled invoices remain in history. If previously published, they remain visible to applicable tenants. | Accepted | Cancellation does not erase the audit trail or automatically hide the invoice. |
| D-028 | Published invoice and tenancy records are not hard-deleted in the MVP. | Accepted | Use archive, end, cancel or tombstone operations instead of destructive deletion. |
| D-029 | Attachments may be freely removed before invoice publication. After publication, normal owner deletion is blocked. | Accepted | Published evidence is retained; an admin emergency-removal operation requires a reason and audit entry. |
| D-030 | Operational notifications are in-app only in the MVP. | Accepted | Email/SMS reminders for invoices are post-MVP. |
| D-031 | Supported locales are `pl` and `en`; the default locale is `pl`. | Accepted | Routes and locale selection begin with Polish by default. |
| D-032 | Default currency is configurable; the initial deployment default is `PLN`. | Accepted | Currency remains stored as an ISO 4217 code per invoice. |
| D-033 | Unauthorized access to a resource outside the user’s visibility returns `404`; a forbidden action on a visible resource returns `403`. | Accepted | This reduces resource enumeration while preserving meaningful authorization errors. |
| D-034 | The system uses date-only business rules for tenancy and invoice billing periods. | Accepted | Due-date comparisons use the configured application clock and deployment time zone. |

## 3. Role definitions

### ADMIN

Responsible for platform administration, user lifecycle, dictionaries, system settings, audit inspection and support operations. Admin access is global, but support writes must be auditable and must not be performed through impersonation.

### OWNER

Responsible for owned properties, tenant assignments, invoice creation, publication, file upload and payment-status management. Ownership is checked through the `property_owners` relation.

### TENANT

Receives read-only access to assigned properties and to published invoices applicable to the tenant’s tenancy period. A tenant may manage only their own profile, password and notification read state.

## 4. Deferred post-MVP decisions

| ID | Topic | Deferred direction |
|---|---|---|
| P-001 | Multi-owner management in the UI | Add co-owner invitations, ownership shares and primary-owner transfer. |
| P-002 | Tenant payment review workflow | Consider “reported paid” and owner verification states. |
| P-003 | Operational email notifications | Add configurable email reminders after in-app notifications are stable. |
| P-004 | File retention automation | Add retention periods and purge jobs when legal and operational requirements are defined. |
| P-005 | User impersonation | Reconsider only with explicit consent, strong audit and privacy safeguards. |
| P-006 | Public registration | Keep disabled unless a future product model requires it. |
| P-007 | OCR and automated invoice extraction | Phase 3 feature. |
| P-008 | Online payments and bank reconciliation | Phase 4 feature. |
| P-009 | Multi-organization SaaS tenancy | Post-MVP architecture extension. |

## 5. Change procedure

A decision change requires:

1. a new or amended decision entry,
2. an explanation of impact,
3. updates to requirements and diagrams,
4. updates to the ERD when the data model changes,
5. updates to the implementation plan when sequencing or scope changes.
