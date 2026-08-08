package pl.propertyrentalmanager.auth.security;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public record CurrentUser(
        UUID userId,
        String email,
        String fullName,
        String status,
        String preferredLocale,
        Set<String> roles,
        int authVersion
) implements Serializable {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
