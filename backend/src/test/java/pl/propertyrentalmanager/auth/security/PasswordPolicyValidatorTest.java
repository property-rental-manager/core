package pl.propertyrentalmanager.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyValidatorTest {

    private PasswordPolicyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordPolicyValidator();
    }

    @Test
    @DisplayName("Should accept valid password of 12+ characters")
    void shouldAcceptValidPassword() {
        assertThatCode(() -> validator.validatePassword("ValidPassword123!"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject password shorter than 12 characters")
    void shouldRejectShortPassword() {
        assertThatThrownBy(() -> validator.validatePassword("Short123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 12 characters");
    }

    @Test
    @DisplayName("Should reject empty or null password")
    void shouldRejectEmptyPassword() {
        assertThatThrownBy(() -> validator.validatePassword(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validator.validatePassword(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject password change when new password equals current password")
    void shouldRejectSamePasswordChange() {
        assertThatThrownBy(() -> validator.validatePasswordChange("Password123456!", "Password123456!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be identical");
    }
}
