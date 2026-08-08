package pl.propertyrentalmanager.common.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.propertyrentalmanager.auth.service.RefreshTokenService;
import pl.propertyrentalmanager.common.exception.RateLimitExceededException;
import pl.propertyrentalmanager.common.exception.ResourceConflictException;
import pl.propertyrentalmanager.common.exception.ResourceNotFoundException;
import pl.propertyrentalmanager.common.web.RequestIdFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(), error.getCode(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return buildResponse(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), fieldErrors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.MALFORMED_REQUEST, "Malformed JSON request", Collections.emptyList(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ApiFieldError fieldError = new ApiFieldError(ex.getParameterName(), "MissingParameter", ex.getMessage());
        return buildResponse(ErrorCode.INVALID_PARAMETER, ex.getMessage(), List.of(fieldError), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceConflict(ResourceConflictException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.CONFLICT, ex.getMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        ApiErrorResponse body = createErrorResponse(ErrorCode.RATE_LIMIT_EXCEEDED, ex.getMessage(), Collections.emptyList(), request);
        return new ResponseEntity<>(body, headers, ErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus());
    }

    @ExceptionHandler(RefreshTokenService.TokenException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenException(RefreshTokenService.TokenException ex, HttpServletRequest request) {
        return buildResponse(ex.getErrorCode(), ex.getMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_CREDENTIALS.getDefaultMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorCode code = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("password")
                ? ErrorCode.PASSWORD_POLICY_VIOLATION
                : ErrorCode.INVALID_PARAMETER;
        return buildResponse(code, ex.getMessage(), Collections.emptyList(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred on path [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), Collections.emptyList(), request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(ErrorCode errorCode, String message, List<ApiFieldError> fieldErrors, HttpServletRequest request) {
        ApiErrorResponse body = createErrorResponse(errorCode, message, fieldErrors, request);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    private ApiErrorResponse createErrorResponse(ErrorCode errorCode, String message, List<ApiFieldError> fieldErrors, HttpServletRequest request) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiErrorResponse.of(
                errorCode,
                message,
                fieldErrors,
                requestId,
                request.getRequestURI()
        );
    }
}
