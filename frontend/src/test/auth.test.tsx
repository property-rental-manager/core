import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "../auth/AuthProvider";
import { LoginPage } from "../pages/auth/LoginPage";
import { ThemeProvider } from "../themes/ThemeProvider";
import { ThemeToggle } from "../components/common/ThemeToggle";
import { clearMemoryToken, getMemoryToken, refreshSingleFlight, setMemoryToken } from "../api/apiClient";
import "../i18n/i18n";

describe("Stage 5 Frontend Auth & API Client Foundation", () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = new QueryClient({
            defaultOptions: { queries: { retry: false } },
        });
        clearMemoryToken();
        localStorage.clear();
        sessionStorage.clear();
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
    });

    it("verifies access token is never saved in localStorage or sessionStorage", () => {
        setMemoryToken("test-secret-jwt-token");
        expect(getMemoryToken()).toBe("test-secret-jwt-token");
        expect(localStorage.getItem("accessToken")).toBeNull();
        expect(sessionStorage.getItem("accessToken")).toBeNull();
    });

    it("renders LoginPage form inputs and submit button", async () => {
        vi.stubGlobal(
            "fetch",
            vi.fn().mockImplementation((url: string) => {
                if (url.includes("/auth/csrf")) {
                    return Promise.resolve(
                        new Response(JSON.stringify({ headerName: "X-XSRF-TOKEN", token: "csrf-val" }), {
                            status: 200,
                            headers: { "Content-Type": "application/json" },
                        }),
                    );
                }
                if (url.includes("/auth/refresh")) {
                    return Promise.resolve(new Response("", { status: 401 }));
                }
                return Promise.reject(new Error("Unknown route"));
            }),
        );

        render(
            <QueryClientProvider client={queryClient}>
                <ThemeProvider>
                    <AuthProvider>
                        <MemoryRouter initialEntries={["/pl/login"]}>
                            <LoginPage />
                        </MemoryRouter>
                    </AuthProvider>
                </ThemeProvider>
            </QueryClientProvider>,
        );

        await waitFor(() => {
            expect(screen.getByLabelText(/Adres e-mail|Email address/i)).toBeInTheDocument();
        });

        expect(screen.getByLabelText(/Hasło|Password/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Zaloguj się|Sign in/i })).toBeInTheDocument();
    });

    it("handles invalid login credentials and displays alert", async () => {
        vi.stubGlobal(
            "fetch",
            vi.fn().mockImplementation((url: string) => {
                if (url.includes("/auth/csrf")) {
                    return Promise.resolve(
                        new Response(JSON.stringify({ headerName: "X-XSRF-TOKEN", token: "csrf-val" }), { status: 200 }),
                    );
                }
                if (url.includes("/auth/refresh")) {
                    return Promise.resolve(new Response("", { status: 401 }));
                }
                if (url.includes("/auth/login")) {
                    return Promise.resolve(
                        new Response(
                            JSON.stringify({
                                code: "INVALID_CREDENTIALS",
                                message: "Bad credentials",
                            }),
                            { status: 401, headers: { "Content-Type": "application/json" } },
                        ),
                    );
                }
                return Promise.reject(new Error("Unknown route"));
            }),
        );

        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <ThemeProvider>
                    <AuthProvider>
                        <MemoryRouter initialEntries={["/pl/login"]}>
                            <LoginPage />
                        </MemoryRouter>
                    </AuthProvider>
                </ThemeProvider>
            </QueryClientProvider>,
        );

        await waitFor(() => {
            expect(screen.getByLabelText(/Adres e-mail|Email address/i)).toBeInTheDocument();
        });

        await user.type(screen.getByLabelText(/Adres e-mail|Email address/i), "wrong@example.com");
        await user.type(screen.getByLabelText(/Hasło|Password/i), "WrongPassword123!");
        await user.click(screen.getByRole("button", { name: /Zaloguj się|Sign in/i }));

        await waitFor(() => {
            expect(screen.getByRole("alert")).toBeInTheDocument();
            expect(screen.getByText(/Nieprawidłowy adres e-mail lub hasło|Invalid email address or password/i)).toBeInTheDocument();
        });
    });

    it("executes single-flight refresh queue without duplicate requests", async () => {
        let refreshCount = 0;
        vi.stubGlobal(
            "fetch",
            vi.fn().mockImplementation((url: string) => {
                if (url.includes("/auth/csrf")) {
                    return Promise.resolve(new Response(JSON.stringify({ token: "csrf-123" }), { status: 200 }));
                }
                if (url.includes("/auth/refresh")) {
                    refreshCount++;
                    return new Promise((resolve) =>
                        setTimeout(
                            () =>
                                resolve(
                                    new Response(
                                        JSON.stringify({
                                            accessToken: "new-single-flight-token",
                                            tokenType: "Bearer",
                                            user: { email: "admin@example.com" },
                                        }),
                                        { status: 200, headers: { "Content-Type": "application/json" } },
                                    ),
                                ),
                            50,
                        ),
                    );
                }
                return Promise.reject(new Error("Unknown route"));
            }),
        );

        const p1 = refreshSingleFlight();
        const p2 = refreshSingleFlight();
        const p3 = refreshSingleFlight();

        const [t1, t2, t3] = await Promise.all([p1, p2, p3]);

        expect(t1).toBe("new-single-flight-token");
        expect(t2).toBe("new-single-flight-token");
        expect(t3).toBe("new-single-flight-token");
        expect(refreshCount).toBe(1);
    });

    it("toggles theme between light and dark mode", async () => {
        render(
            <ThemeProvider>
                <ThemeToggle />
            </ThemeProvider>,
        );

        const toggleBtn = screen.getByRole("button", { name: /Toggle Theme/i });
        expect(document.documentElement.getAttribute("data-theme")).toBe("light");

        fireEvent.click(toggleBtn);
        expect(document.documentElement.getAttribute("data-theme")).toBe("dark");

        fireEvent.click(toggleBtn);
        expect(document.documentElement.getAttribute("data-theme")).toBe("light");
    });
});
