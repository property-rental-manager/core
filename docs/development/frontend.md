# Frontend Development Guide — Property Rental Manager

This guide covers setting up, developing, testing, and building the frontend module (`core/frontend/`).

---

## 1. Prerequisites

- **Node.js**: v20+ or v22+
- **npm**: v10+
- **Backend API**: Running on `http://localhost:8080` (or configured via `VITE_API_BASE_URL`)

---

## 2. Installation & Setup

Navigate to `core/frontend/`:

```bash
cd frontend
npm install
```

---

## 3. Environment Configuration

The frontend uses Vite environment variables defined in `core/frontend/.env.example` or root `.env`:

```ini
VITE_API_BASE_URL=/api/v1
VITE_DEFAULT_LOCALE=pl
VITE_SUPPORTED_LOCALES=pl,en
VITE_APP_NAME=Property Rental Manager
```

> [!WARNING]
> **Security Notice:** All `VITE_*` environment variables are public and bundled into client JavaScript. **NEVER** place secret keys (e.g. `JWT_SECRET_KEY`, database credentials, or admin passwords) in `VITE_*` environment variables.

---

## 4. Running Development Server

Start the Vite development server on `http://localhost:5173`:

```bash
cd frontend
npm run dev
```

During development, Vite proxies requests from `/api` to `http://localhost:8080` to prevent CORS issues while preserving cookie credentials (`credentials: "include"`).

---

## 5. Security & Session Management Policy

1. **Access Token:**
   - Saved **strictly in application memory** inside `AuthProvider` state (`setMemoryToken()`).
   - **NEVER** stored in `localStorage`, `sessionStorage`, `IndexedDB`, or client-accessible cookies.

2. **Refresh Token:**
   - Managed as an `HttpOnly`, `SameSite=Lax` cookie (`prm_refresh_token`) set and rotated exclusively by the backend.

3. **CSRF Protection:**
   - Fetches CSRF token via `GET /api/v1/auth/csrf`.
   - Injects `X-XSRF-TOKEN` header on non-GET requests (`POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, etc.).

4. **Single-Flight Refresh Queue:**
   - If an API request receives HTTP 401, a single background refresh request is executed via `refreshSingleFlight()`.
   - Concurrent requests wait on the same Promise and retry once upon success.
   - If refresh fails, auth state transitions to `UNAUTHENTICATED`.

---

## 6. Testing & Quality Scripts

```bash
# Run ESLint linter
npm run lint

# Run Vitest test suite
npm run test

# Run production build & TypeScript validation
npm run build
```
