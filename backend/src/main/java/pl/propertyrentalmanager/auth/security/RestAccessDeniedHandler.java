package pl.propertyrentalmanager.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import pl.propertyrentalmanager.common.error.ApiErrorResponse;
import pl.propertyrentalmanager.common.error.ErrorCode;
import pl.propertyrentalmanager.common.web.RequestIdFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (requestId == null) {
            requestId = response.getHeader(RequestIdFilter.HEADER_NAME);
        }

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                errorCode,
                errorCode.getDefaultMessage(),
                Collections.emptyList(),
                requestId,
                request.getRequestURI()
        );

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
