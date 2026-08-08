package pl.propertyrentalmanager.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
    }

    @Test
    @DisplayName("Should correctly evaluate roles for CurrentUser principal")
    void shouldEvaluateRolesCorrectly() {
        CurrentUser adminUser = new CurrentUser(
                UUID.randomUUID(), "admin@example.com", "Admin", "ACTIVE", "pl", Set.of("ADMIN", "OWNER"), 0
        );
        CurrentUser tenantUser = new CurrentUser(
                UUID.randomUUID(), "tenant@example.com", "Tenant", "ACTIVE", "pl", Set.of("TENANT"), 0
        );

        assertThat(permissionService.isAdmin(adminUser)).isTrue();
        assertThat(permissionService.canAccessAdminArea(adminUser)).isTrue();
        assertThat(permissionService.hasRole(adminUser, "OWNER")).isTrue();
        assertThat(permissionService.hasAnyRole(adminUser, Set.of("OWNER", "TENANT"))).isTrue();

        assertThat(permissionService.isAdmin(tenantUser)).isFalse();
        assertThat(permissionService.canAccessAdminArea(tenantUser)).isFalse();
        assertThat(permissionService.hasRole(tenantUser, "ADMIN")).isFalse();
    }
}
