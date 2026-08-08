package pl.propertyrentalmanager.auth.security;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service("permissionService")
public class PermissionService {

    public boolean isAdmin(CurrentUser user) {
        return user != null && user.isAdmin();
    }

    public boolean hasRole(CurrentUser user, String roleCode) {
        return user != null && user.hasRole(roleCode);
    }

    public boolean hasAnyRole(CurrentUser user, Set<String> roles) {
        if (user == null || user.roles() == null || roles == null) {
            return false;
        }
        return user.roles().stream().anyMatch(roles::contains);
    }

    public boolean canAccessAdminArea(CurrentUser user) {
        return isAdmin(user);
    }
}
