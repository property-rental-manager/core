package pl.propertyrentalmanager.auth.web.dto;

import java.util.Set;
import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String email,
        String fullName,
        String status,
        String preferredLocale,
        Set<String> roles
) {}
