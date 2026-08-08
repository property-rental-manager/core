package pl.propertyrentalmanager.auth.web.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummaryDto user
) {}
