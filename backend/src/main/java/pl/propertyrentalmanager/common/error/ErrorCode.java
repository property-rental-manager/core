package pl.propertyrentalmanager.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed for request parameters"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Malformed JSON or invalid request format"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid request parameter value"),
    CURRENT_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "The current password provided is incorrect"),
    PASSWORD_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "Password does not satisfy the security policy requirements"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "The access token provided is invalid"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "The access token provided has expired"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "The refresh token provided is invalid"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "The refresh token provided has expired"),
    REFRESH_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "Refresh token reuse detected; all sessions revoked"),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "User account is disabled"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access is denied"),
    CSRF_TOKEN_INVALID(HttpStatus.FORBIDDEN, "CSRF token is missing or invalid"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found"),
    CONFLICT(HttpStatus.CONFLICT, "Resource conflict occurred"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests; rate limit exceeded"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
