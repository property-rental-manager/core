package pl.propertyrentalmanager;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should execute Flyway migration successfully and verify table creation")
    void shouldVerifyDatabaseSchemaTablesExist() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String.class
        );

        assertThat(tables).contains("users", "roles", "user_roles", "user_profiles");
    }

    @Test
    @DisplayName("Should verify seed roles exist in roles table")
    void shouldVerifySeedRolesExist() {
        List<String> roleCodes = jdbcTemplate.queryForList(
                "SELECT code FROM roles ORDER BY code",
                String.class
        );

        assertThat(roleCodes).containsExactlyInAnyOrder("ADMIN", "OWNER", "TENANT");
    }

    @Test
    @DisplayName("Should confirm re-running Flyway migration does not cause errors")
    void shouldConfirmReRunningMigrationIsIdempotent() {
        assertDoesNotThrow(() -> {
            MigrateResult result = flyway.migrate();
            assertThat(result.migrationsExecuted).isZero();
        });
    }
}
