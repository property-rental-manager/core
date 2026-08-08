export type Role = "ADMIN" | "OWNER" | "TENANT";

export type UserStatus = "INVITED" | "ACTIVE" | "DISABLED";

export interface User {
    id: string;
    email: string;
    fullName: string;
    status: UserStatus;
    preferredLocale: string;
    roles: Role[];
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}

export interface CsrfTokenResponse {
    headerName: string;
    parameterName: string;
    token: string;
}

export interface ApiFieldError {
    field: string;
    code?: string;
    message: string;
}

export type ErrorCode =
    | "VALIDATION_ERROR"
    | "MALFORMED_REQUEST"
    | "INVALID_PARAMETER"
    | "INVALID_CREDENTIALS"
    | "AUTHENTICATION_REQUIRED"
    | "ACCESS_DENIED"
    | "TOKEN_INVALID"
    | "TOKEN_EXPIRED"
    | "REFRESH_TOKEN_INVALID"
    | "REFRESH_TOKEN_EXPIRED"
    | "REFRESH_TOKEN_REUSE_DETECTED"
    | "RATE_LIMIT_EXCEEDED"
    | "PASSWORD_POLICY_VIOLATION"
    | "RESOURCE_NOT_FOUND"
    | "CONFLICT"
    | "INTERNAL_ERROR"
    | "UNKNOWN_ERROR";

export interface ApiErrorResponse {
    code: ErrorCode;
    message: string;
    fieldErrors: ApiFieldError[];
    requestId: string;
    timestamp: string;
    path: string;
}

export type AuthStatus = "INITIALIZING" | "AUTHENTICATED" | "UNAUTHENTICATED";

export type SupportedLocale = "pl" | "en";

export type Theme = "light" | "dark";
