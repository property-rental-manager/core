package pl.propertyrentalmanager.auth.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 128;

    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public void validatePasswordChange(String currentPassword, String newPassword) {
        validatePassword(newPassword);
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("New password cannot be identical to the current password");
        }
    }
}
