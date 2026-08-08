import { apiRequest } from "./apiClient";
import type {
    ChangePasswordRequest,
    CsrfTokenResponse,
    LoginRequest,
    LoginResponse,
    User,
} from "../types/auth";

export const authApi = {
    fetchCsrf: async (): Promise<CsrfTokenResponse> => {
        return apiRequest<CsrfTokenResponse>("/auth/csrf", { method: "GET" });
    },

    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        return apiRequest<LoginResponse>("/auth/login", {
            method: "POST",
            body: JSON.stringify(credentials),
        });
    },

    refresh: async (): Promise<LoginResponse> => {
        return apiRequest<LoginResponse>("/auth/refresh", {
            method: "POST",
        });
    },

    logout: async (): Promise<void> => {
        return apiRequest<void>("/auth/logout", {
            method: "POST",
        });
    },

    getMe: async (): Promise<User> => {
        return apiRequest<User>("/me", {
            method: "GET",
        });
    },

    changePassword: async (dto: ChangePasswordRequest): Promise<void> => {
        return apiRequest<void>("/me/password", {
            method: "POST",
            body: JSON.stringify(dto),
        });
    },
};
