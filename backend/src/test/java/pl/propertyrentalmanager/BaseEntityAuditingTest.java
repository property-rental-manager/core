package pl.propertyrentalmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import pl.propertyrentalmanager.test.TestUserRepository;
import pl.propertyrentalmanager.user.UserEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class BaseEntityAuditingTest {

    @Autowired
    private TestUserRepository userRepository;

    @Test
    @DisplayName("Should automatically populate createdAt and updatedAt Instant fields on save")
    void shouldAutomaticallyPopulateAuditingTimestamps() {
        UserEntity user = new UserEntity();
        user.setEmail("auditing-test@example.com");
        user.setPasswordHash("hashed_password");
        user.setFullName("Auditing Test User");
        user.setStatus("ACTIVE");
        user.setPreferredLocale("pl");

        UserEntity savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isEqualTo(savedUser.getUpdatedAt());

        userRepository.deleteById(savedUser.getId());
    }
}
