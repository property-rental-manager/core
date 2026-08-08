import type { ApiErrorResponse, CsrfTokenResponse, ErrorCode } from "../types/auth";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

let memoryAccessToken: string | null = null;
let csrfTokenCache: string | null = null;
let singleFlightRefreshPromise: Promise<string> | null = null;
let onUnauthenticatedCallback: (() => void) | null = null;

export function setMemoryToken(token: string | null): void {
    memoryAccessToken = token;
}

export function getMemoryToken(): string | null {
    return memoryAccessToken;
}

export function clearMemoryToken(): void {
    memoryAccessToken = null;
}

export function setCsrfTokenCache(token: string | null): void {
    csrfTokenCache = token;
}

export function setOnUnauthenticatedCallback(callback: () => void): void {
    onUnauthenticatedCallback = callback;
}

export class ApiError extends Error {
    public readonly status: number;
    public readonly code: ErrorCode;
    public readonly fieldErrors: ApiErrorResponse["fieldErrors"];
    public readonly requestId?: string;
    public readonly path?: string;

    constructor(
        status: number,
        code: ErrorCode,
        message: string,
        fieldErrors: ApiErrorResponse["fieldErrors"] = [],
        requestId?: string,
        path?: string,
    ) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors;
        this.requestId = requestId;
        this.path = path;
    }
}

export async function fetchCsrfToken(): Promise<string> {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/csrf`, {
            method: "GET",
            credentials: "include",
        });
        if (res.ok) {
            const data: CsrfTokenResponse = await res.json();
            if (data.token) {
                csrfTokenCache = data.token;
                return data.token;
            }
        }
    } catch {
        // Fallback to cookie read if GET /auth/csrf fails
    }

    const cookieMatch = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
    if (cookieMatch) {
        const val = decodeURIComponent(cookieMatch[1]);
        csrfTokenCache = val;
        return val;
    }

    return csrfTokenCache ?? "";
}

async function getXsrfHeaderValue(): Promise<string> {
    if (csrfTokenCache) {
        return csrfTokenCache;
    }
    return fetchCsrfToken();
}

export async function apiRequest<T>(
    path: string,
    options: RequestInit = {},
    isRetry = false,
): Promise<T> {
    const url = `${API_BASE_URL}${path}`;
    const headers = new Headers(options.headers || {});

    if (!headers.has("Content-Type") && !(options.body instanceof FormData)) {
        headers.set("Content-Type", "application/json");
    }

    if (memoryAccessToken) {
        headers.set("Authorization", `Bearer ${memoryAccessToken}`);
    }

    const method = (options.method || "GET").toUpperCase();
    if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
        const xsrf = await getXsrfHeaderValue();
        if (xsrf) {
            headers.set("X-XSRF-TOKEN", xsrf);
        }
    }

    const response = await fetch(url, {
        ...options,
        headers,
        credentials: "include",
    });

    if (!response.ok) {
        let errorData: Partial<ApiErrorResponse> = {};
        try {
            errorData = await response.json();
        } catch {
            // Non-JSON response
        }

        const code: ErrorCode = (errorData.code as ErrorCode) ?? "UNKNOWN_ERROR";
        const message = errorData.message || `Request failed with status ${response.status}`;

        if (response.status === 401 && !isRetry && !path.startsWith("/auth/login") && !path.startsWith("/auth/refresh")) {
            try {
                const newAccessToken = await refreshSingleFlight();
                setMemoryToken(newAccessToken);
                return apiRequest<T>(path, options, true);
            } catch {
                clearMemoryToken();
                if (onUnauthenticatedCallback) {
                    onUnauthenticatedCallback();
                }
                throw new ApiError(401, code, message, errorData.fieldErrors, errorData.requestId, errorData.path);
            }
        }

        throw new ApiError(
            response.status,
            code,
            message,
            errorData.fieldErrors || [],
            errorData.requestId,
            errorData.path,
        );
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}

export function refreshSingleFlight(): Promise<string> {
    if (singleFlightRefreshPromise) {
        return singleFlightRefreshPromise;
    }

    singleFlightRefreshPromise = (async () => {
        try {
            const xsrf = await getXsrfHeaderValue();
            const headers: Record<string, string> = {
                "Content-Type": "application/json",
            };
            if (xsrf) {
                headers["X-XSRF-TOKEN"] = xsrf;
            }

            const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
                method: "POST",
                headers,
                credentials: "include",
            });

            if (!response.ok) {
                clearMemoryToken();
                throw new Error("Refresh failed");
            }

            const data = await response.json();
            setMemoryToken(data.accessToken);
            return data.accessToken as string;
        } finally {
            singleFlightRefreshPromise = null;
        }
    })();

    return singleFlightRefreshPromise;
}