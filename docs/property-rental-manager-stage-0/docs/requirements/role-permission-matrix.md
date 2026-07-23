# Property Rental Manager — Role and Permission Matrix

> **Document status:** Approved proposal for Stage 0 review  
> **Authorization rule:** frontend visibility never replaces backend enforcement.

## 1. Matrix

| Capability | ADMIN | OWNER | TENANT |
|---|---|---|---|
| Sign in and sign out | Own account | Own account | Own account |
| Change own password | Allowed | Allowed | Allowed |
| View own profile | Allowed | Allowed | Allowed |
| Create admin account | Allowed | No | No |
| Create owner account | Allowed | No | No |
| Create tenant account | Allowed | Invitation flow only | No |
| Invite tenant | Support action | Only for owned property | No |
| List all users | Allowed | No | No |
| Change user role/status | Allowed | No | No |
| Reset another user’s password | Allowed | No | No |
| View all properties | Allowed | No | No |
| Create property | Support action or own admin-created resource | Allowed; creator becomes primary owner | No |
| View property | All | Owned only | Assigned only |
| Edit property | All, audited | Owned only | No |
| Archive/restore property | All, audited | Owned only | No |
| Manage owner relations | Support action | Primary-owner UI deferred | No |
| View tenancies | All | Owned property only | Own tenancy only |
| Assign tenant | Support action | Owned property only | No |
| End/cancel tenancy | Support action | Owned property only | No |
| Create invoice | Support action | Owned property only | No |
| Edit draft invoice | All, audited | Owned property only | No |
| Publish invoice | All, audited | Owned property only | No |
| Unpublish pending invoice | All, audited | Owned property only | No |
| View invoice | All | Owned property only | Published and tenancy-applicable only |
| Mark invoice paid | All, audited | Owned property only | No |
| Cancel invoice | All, audited | Owned property only | No |
| Upload attachment | All, audited | Owned property invoice only | No |
| Remove attachment before publication | All, audited | Owned property invoice only | No |
| Remove attachment after publication | Emergency admin action with reason | No | No |
| Download attachment | All | Owned property invoice only | Published and tenancy-applicable only |
| View notifications | Own and operational support | Own | Own |
| Mark notification read | Own | Own | Own |
| Manage dictionaries/settings | Allowed | No | No |
| View global audit logs | Allowed | No |
| View own-domain audit events | Optional future feature | Not in MVP | No |
| Impersonate another user | No | No | No |

## 2. Resource-level authorization rules

### 2.1 Property view

A property is visible when at least one rule is true:

```text
ADMIN
OR ownership relation exists for OWNER
OR tenancy relation exists for TENANT
```

For tenant access, an ended tenancy may still allow the property to appear in a historical section, but it does not grant access to invoices outside that tenancy period.

### 2.2 Property mutation

```text
ADMIN
OR active ownership relation exists for OWNER
```

A tenant is never allowed to mutate property data.

### 2.3 Invoice view

```text
ADMIN
OR owner owns invoice.property
OR (
    invoice.isPublished
    AND tenant has an applicable tenancy for invoice
)
```

### 2.4 Invoice mutation

```text
ADMIN
OR owner owns invoice.property
```

Additional operation-specific state checks are mandatory. Ownership alone does not permit an illegal state transition.

### 2.5 File download

A file is downloadable only when the user can view the parent invoice and the file has not been tombstoned or removed.

## 3. HTTP behavior

| Situation | Response |
|---|---|
| User is unauthenticated | `401 Unauthorized` |
| Resource is outside user visibility | `404 Not Found` |
| Resource is visible but action is not permitted | `403 Forbidden` |
| Operation violates business state | `409 Conflict` or stable domain error |
| Input is invalid | `400 Bad Request` with field errors |

## 4. Required security tests

- owner requests another owner’s property,
- owner edits another owner’s invoice,
- tenant changes property ID in URL,
- tenant changes invoice ID in URL,
- tenant downloads another tenant’s file,
- new tenant requests an invoice from before tenancy,
- former tenant requests an invoice from after tenancy,
- tenant requests unpublished invoice,
- disabled user attempts login,
- non-admin attempts admin endpoint,
- owner attempts post-publication file deletion,
- admin support write creates an audit event.
