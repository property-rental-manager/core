package pl.propertyrentalmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ClockConfigTest {

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("Should inject system UTC Clock by default")
    void shouldInjectClockBean() {
        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Should support fixed clock substitution for deterministic testing")
    void shouldSupportFixedClockSubstitution() {
        Instant fixedInstant = Instant.parse("2026-08-06T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

        assertThat(Instant.now(fixedClock)).isEqualTo(fixedInstant);
    }
}
