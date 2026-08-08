import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "../routes/ProtectedRoute";
import { RoleRoute } from "../routes/RoleRoute";
import { ForbiddenPage } from "../pages/errors/ForbiddenPage";
import { NotFoundPage } from "../pages/errors/NotFoundPage";
import { AuthContext } from "../auth/AuthContext";
import type { User } from "../types/auth";
import "../i18n/i18n";

describe("Stage 5 Frontend Routing & Guards Foundation", () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    const mockAuthContext = (overrides = {}) => ({
        user: null,
        accessToken: null,
        status: "UNAUTHENTICATED" as const,
        isAuthenticated: false,
        isInitializing: false,
        login: vi.fn(),
        logout: vi.fn(),
        refreshSession: vi.fn(),
        changePassword: vi.fn(),
        ...overrides,
    });

    it("renders loading bootstrap screen during INITIALIZING status", () => {
        render(
            <AuthContext.Provider value={mockAuthContext({ isInitializing: true, status: "INITIALIZING" })}>
                <MemoryRouter initialEntries={["/pl/dashboard"]}>
                    <Routes>
                        <Route path="/:locale" element={<ProtectedRoute />}>
                            <Route path="dashboard" element={<div>Protected Dashboard</div>} />
                        </Route>
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );

        expect(screen.getByText(/Loading Property Rental Manager/i)).toBeInTheDocument();
        expect(screen.queryByText("Protected Dashboard")).toBeNull();
    });

    it("redirects unauthenticated users from ProtectedRoute to /pl/login", () => {
        render(
            <AuthContext.Provider value={mockAuthContext({ isAuthenticated: false, status: "UNAUTHENTICATED" })}>
                <MemoryRouter initialEntries={["/pl/dashboard"]}>
                    <Routes>
                        <Route path="/:locale" element={<ProtectedRoute />}>
                            <Route path="dashboard" element={<div>Protected Dashboard</div>} />
                        </Route>
                        <Route path="/:locale/login" element={<div>Login Page Target</div>} />
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );

        expect(screen.getByText("Login Page Target")).toBeInTheDocument();
        expect(screen.queryByText("Protected Dashboard")).toBeNull();
    });

    it("allows authenticated users through ProtectedRoute", () => {
        const mockUser: User = {
            id: "u1",
            email: "admin@example.com",
            fullName: "Admin User",
            status: "ACTIVE",
            preferredLocale: "pl",
            roles: ["ADMIN"],
        };

        render(
            <AuthContext.Provider value={mockAuthContext({ isAuthenticated: true, status: "AUTHENTICATED", user: mockUser })}>
                <MemoryRouter initialEntries={["/pl/dashboard"]}>
                    <Routes>
                        <Route path="/:locale" element={<ProtectedRoute />}>
                            <Route path="dashboard" element={<div>Protected Dashboard</div>} />
                        </Route>
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );

        expect(screen.getByText("Protected Dashboard")).toBeInTheDocument();
    });

    it("RoleRoute permits users with allowed role and blocks users without allowed role", () => {
        const adminUser: User = {
            id: "u1",
            email: "admin@example.com",
            fullName: "Admin User",
            status: "ACTIVE",
            preferredLocale: "pl",
            roles: ["ADMIN"],
        };

        const tenantUser: User = {
            id: "u2",
            email: "tenant@example.com",
            fullName: "Tenant User",
            status: "ACTIVE",
            preferredLocale: "pl",
            roles: ["TENANT"],
        };

        const { rerender } = render(
            <AuthContext.Provider value={mockAuthContext({ user: adminUser })}>
                <MemoryRouter initialEntries={["/pl/admin"]}>
                    <Routes>
                        <Route path="/:locale" element={<RoleRoute allowedRoles={["ADMIN"]} />}>
                            <Route path="admin" element={<div>Admin Area</div>} />
                        </Route>
                        <Route path="/:locale/403" element={<div>403 Forbidden</div>} />
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );

        expect(screen.getByText("Admin Area")).toBeInTheDocument();

        rerender(
            <AuthContext.Provider value={mockAuthContext({ user: tenantUser })}>
                <MemoryRouter initialEntries={["/pl/admin"]}>
                    <Routes>
                        <Route path="/:locale" element={<RoleRoute allowedRoles={["ADMIN"]} />}>
                            <Route path="admin" element={<div>Admin Area</div>} />
                        </Route>
                        <Route path="/:locale/403" element={<div>403 Forbidden Target</div>} />
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );

        expect(screen.getByText("403 Forbidden Target")).toBeInTheDocument();
    });

    it("renders ForbiddenPage 403 correctly", () => {
        render(
            <MemoryRouter initialEntries={["/pl/403"]}>
                <Routes>
                    <Route path="/:locale/403" element={<ForbiddenPage />} />
                </Routes>
            </MemoryRouter>,
        );

        expect(screen.getByText(/Brak dostępu \(403\)|Access Denied \(403\)/i)).toBeInTheDocument();
    });

    it("renders NotFoundPage 404 correctly", () => {
        render(
            <MemoryRouter initialEntries={["/pl/404"]}>
                <Routes>
                    <Route path="/:locale/404" element={<NotFoundPage />} />
                </Routes>
            </MemoryRouter>,
        );

        expect(screen.getByText(/Strona nie została znaleziona \(404\)|Page Not Found \(404\)/i)).toBeInTheDocument();
    });
});
