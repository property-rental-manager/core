package pl.propertyrentalmanager.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.propertyrentalmanager.common.exception.ResourceConflictException;
import pl.propertyrentalmanager.common.exception.ResourceNotFoundException;
import pl.propertyrentalmanager.common.web.RequestIdFilter;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(
                        error.getField(),
                        error.getCode(),
                        error.getDefaultMessage()
                ))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Request validation failed",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiFieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiFieldError(
                        violation.getPropertyPath().toString(),
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        violation.getMessage()
                ))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Constraint validation failed",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.MALFORMED_REQUEST,
                "Malformed JSON request body",
                List.of(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_PARAMETER,
                message,
                List.of(),
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_PARAMETER,
                message,
                List.of(),
                request
        );
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : "Resource not found",
                List.of(),
                request
        );
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ResourceConflictException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                ex.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "Access is denied",
                List.of(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception caught by GlobalExceptionHandler on path: {}", request.getRequestURI(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "An unexpected internal server error occurred",
                List.of(),
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            List<ApiFieldError> fieldErrors,
            HttpServletRequest request
    ) {
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        if (requestId == null) {
            requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        }

        ApiErrorResponse body = ApiErrorResponse.of(
                errorCode,
                message,
                fieldErrors,
                requestId,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }
}
