package pl.propertyrentalmanager.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-08-08T12:00:00Z"), ZoneId.of("UTC"));
        rateLimiter = new LoginRateLimiter(5, 15, fixedClock);
    }

    @Test
    @DisplayName("Should block email after 5 failed attempts")
    void shouldBlockAfterMaxAttempts() {
        String email = "victim@example.com";
        String ip = "192.168.1.100";

        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.isRateLimited(email, ip)).isFalse();
            rateLimiter.recordFailedAttempt(email, ip);
        }

        assertThat(rateLimiter.isRateLimited(email, ip)).isTrue();
        assertThat(rateLimiter.getRetryAfterSeconds(email, ip)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should reset rate limiter on successful login")
    void shouldResetOnSuccess() {
        String email = "victim@example.com";
        String ip = "192.168.1.100";

        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailedAttempt(email, ip);
        }
        assertThat(rateLimiter.isRateLimited(email, ip)).isTrue();

        rateLimiter.resetRateLimit(email, ip);
        assertThat(rateLimiter.isRateLimited(email, ip)).isFalse();
    }
}
