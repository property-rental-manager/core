# Security Architecture: Authentication & Token Management

This document details the security design, token lifecycle, threat mitigation, and authorization foundation implemented in **Stage 4** of **Property Rental Manager**.

---

## 1. Core Principles & Threat Model

1. **Stateless Access Validation:** Short-lived JWT access tokens (15 min TTL) carry user identities, permissions, and `authVersion`. Microservices validate signature, expiration, issuer, audience, and active user status without DB sessions.
2. **Secure Long-Lived Sessions:** Refresh tokens are 256-bit secure random opaque strings transmitted exclusively via `HttpOnly`, `SameSite=Lax` cookies over HTTPS.
3. **Zero Plaintext Token Storage:** Database stores strictly SHA-256 hashes (`token_hash`) of refresh tokens. Database compromise does not leak active session tokens.
4. **Pessimistic Token Rotation & Family Tracking:** Every token refresh revokes the old token (`ROTATED`) and issues a new token in the same `token_family_id`.
5. **Automatic Reuse Detection:** Attempting to refresh with a previously rotated token indicates token theft. The system immediately revokes the entire token family, increments `users.auth_version`, invalidates all active JWT access tokens, and logs a security audit event.
6. **Defense in Depth Rate Limiting:** In-memory Caffeine rate limiter tracks failed login attempts by email and IP (5 failures / 15 minutes). Exceeding limits returns HTTP 429 `RATE_LIMIT_EXCEEDED` with `Retry-After`.
7. **CSRF & CORS Isolation:** Cookie-authenticated endpoints (`/api/v1/auth/refresh`, `/api/v1/auth/logout`) are protected with Double-Submit Cookie CSRF tokens (`X-XSRF-TOKEN`). Bearer token API endpoints ignore CSRF as headers cannot be automatically attached cross-origin by browsers. CORS strictly white-lists allowed origins.

---

## 2. Token Architecture Details

### Access Token (JWT)
- **Algorithm:** HMAC-SHA256 (JJWT 0.12.6)
- **Secret Key:** Min 256 bits (`APP_JWT_SECRET_KEY`)
- **TTL:** 15 minutes (`APP_JWT_ACCESS_TOKEN_EXPIRE_MINUTES`)
- **Claims:**
  - `sub`: User UUID
  - `email`: Normalized email string
  - `roles`: JSON array of string codes (`["ADMIN", "OWNER"]`)
  - `authVersion`: Integer counter matching `users.auth_version`
  - `iss`: Configured issuer (`property-rental-manager`)
  - `aud`: Configured audience (`property-rental-manager-api`)

### Refresh Token (Opaque Cookie)
- **Generation:** 256-bit cryptographic secure random (`SecureRandom`), URL-safe Base64 encoded.
- **Storage:** SHA-256 hash stored in `refresh_tokens.token_hash`.
- **TTL:** 30 days (`APP_JWT_REFRESH_TOKEN_EXPIRE_DAYS`)
- **Cookie Attributes:**
  - Name: `prm_refresh_token`
  - Path: `/api/v1/auth`
  - HttpOnly: `true`
  - SameSite: `Lax`
  - Secure: `true` (prod) / `false` (dev)

---

## 3. Global Invalidation & Password Security

- **Password Policy:** Minimum 12 characters, maximum 128 characters, non-blank, new password must differ from current password. Hashed using BCrypt (strength 12).
- **Password Change / Emergency Revocation:** Changing password or triggering account revocation increments `users.auth_version` and bulk-revokes all active `refresh_tokens`.
- **Immediate Rejection:** `JwtAuthenticationFilter` validates DB `user.authVersion` against JWT `authVersion` claim. Bumping `auth_version` immediately invalidates all active JWT access tokens without waiting for expiration.
