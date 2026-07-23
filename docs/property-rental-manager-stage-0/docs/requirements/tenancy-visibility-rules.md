# Property Rental Manager — Tenancy and Invoice Visibility Rules

> **Document status:** Approved proposal for Stage 0 review

## 1. Purpose

These rules prevent tenants from viewing invoices unrelated to their occupancy while allowing a former tenant to retain access to records that applied during their own tenancy.

## 2. Tenancy interval

A tenancy interval is inclusive:

```text
[start_date, end_date]
```

When `end_date` is null, the interval is open-ended.

## 3. Invoice applicability model

The `tenancy_id` field on an invoice is optional.

### 3.1 Tenancy-specific invoice

When `invoice.tenancy_id` is present:

- the tenancy must belong to the invoice property,
- only the tenant referenced by that tenancy can view the invoice,
- publication is still required.

### 3.2 Property-wide invoice

When `invoice.tenancy_id` is null, the invoice may be visible to multiple tenants. Applicability is determined by the invoice reference period.

Reference period priority:

1. `billing_period_from` and `billing_period_to`,
2. `issue_date` as a single-day period when billing period is absent.

A property-wide invoice is applicable when the invoice reference period overlaps the tenant’s tenancy interval.

## 4. Overlap rule

Two inclusive periods overlap when:

```text
invoice_start <= tenancy_end_or_infinity
AND tenancy_start <= invoice_end
```

Equivalent service rule:

```java
boolean overlaps(
    LocalDate invoiceStart,
    LocalDate invoiceEnd,
    LocalDate tenancyStart,
    LocalDate tenancyEnd
) {
    LocalDate effectiveTenancyEnd = tenancyEnd == null
        ? LocalDate.MAX
        : tenancyEnd;

    return !invoiceStart.isAfter(effectiveTenancyEnd)
        && !tenancyStart.isAfter(invoiceEnd);
}
```

## 5. Complete tenant invoice visibility predicate

```text
authenticated user has TENANT role
AND invoice.isPublished = true
AND invoice status is not DRAFT
AND one of:
    A. invoice.tenancyId belongs to the user
    B. invoice.tenancyId is null and a tenancy for the same property overlaps the invoice reference period
AND attached file is not removed/tombstoned
```

## 6. Decision table

| Scenario | Visible? | Reason |
|---|---:|---|
| Active tenant, published invoice inside tenancy period | Yes | Publication and date applicability satisfied. |
| Active tenant, draft invoice | No | Draft is never visible. |
| Active tenant, unpublished invoice | No | Publication required. |
| New tenant, invoice period before tenancy start | No | No period overlap. |
| Former tenant, invoice period inside ended tenancy | Yes | Historical access is retained for applicable records. |
| Former tenant, invoice period after tenancy end | No | No period overlap. |
| Tenant-specific invoice linked to another tenant’s tenancy | No | Explicit tenancy scope takes precedence. |
| Property-wide invoice overlapping two active tenancies | Yes for both | Shared-property expense applies to both tenancy periods. |
| Cancelled invoice published before cancellation | Yes, when applicable | Cancellation preserves published history. |
| Cancelled draft | No | It was never published. |
| Disabled tenant account | No login access | Account status blocks authentication. |

## 7. Property visibility

- active tenancy: property appears in the active properties section,
- ended tenancy: property may appear in a historical section,
- cancelled invitation/tenancy with no effective occupancy: no tenant property access,
- property visibility does not automatically grant visibility to every invoice.

## 8. Owner visibility

An owner can view and manage tenancy and invoice data only when an active ownership relation exists for the property. Historical ownership behavior may be added later; the MVP treats ended ownership as read-only support data unless an admin intervenes.

## 9. Security behavior

- a tenant requesting a non-visible invoice receives `404`,
- a tenant requesting a non-visible file receives `404`,
- a tenant attempting mutation on a visible invoice receives `403`,
- all checks are evaluated by the backend using authenticated user identity,
- route parameters and frontend state are never trusted as authorization evidence.

## 10. Required database indexes

Recommended indexes for visibility queries:

```text
tenancies(tenant_id, property_id, start_date, end_date, status)
invoices(property_id, tenancy_id, is_published, status)
invoices(property_id, billing_period_from, billing_period_to)
invoice_files(invoice_id)
```

## 11. Required tests

- overlap begins exactly on tenancy start,
- overlap ends exactly on tenancy end,
- invoice ends one day before tenancy start,
- invoice starts one day after tenancy end,
- open-ended active tenancy,
- multiple tenants in one property,
- one tenant in multiple properties,
- tenancy-specific invoice isolation,
- historical former-tenant access,
- unpublished and draft isolation,
- cancelled published history.
