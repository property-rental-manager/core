package pl.propertyrentalmanager.auth.web.dto;

public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
) {}
