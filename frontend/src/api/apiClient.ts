const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

export class ApiError extends Error {
    constructor(
        public readonly status: number,
        public readonly code: string,
        message: string,
    ) {
        super(message);
        this.name = "ApiError";
    }
}

export async function apiRequest<T>(
    path: string,
    options: RequestInit = {},
): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,

        headers: {
            "Content-Type": "application/json",
            ...options.headers,
        },

        credentials: "include",
    });

    if (!response.ok) {
        let body: {
            code?: string;
            message?: string;
        } = {};

        try {
            body = await response.json();
        } catch {
            // Backend może zwrócić odpowiedź bez treści JSON.
        }

        throw new ApiError(
            response.status,
            body.code ?? "UNKNOWN_ERROR",
            body.message ?? "Request failed",
        );
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}