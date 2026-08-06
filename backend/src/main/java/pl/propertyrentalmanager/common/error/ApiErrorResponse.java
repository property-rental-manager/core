package pl.propertyrentalmanager.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        ErrorCode code,
        String message,
        List<ApiFieldError> fieldErrors,
        String requestId,
        Instant timestamp,
        String path
) {
    public static ApiErrorResponse of(ErrorCode code, String message, List<ApiFieldError> fieldErrors, String requestId, String path) {
        return new ApiErrorResponse(
                code,
                message,
                fieldErrors != null ? fieldErrors : List.of(),
                requestId,
                Instant.now(),
                path
        );
    }

    public static ApiErrorResponse of(ErrorCode code, String message, String requestId, String path) {
        return of(code, message, List.of(), requestId, path);
    }
}
