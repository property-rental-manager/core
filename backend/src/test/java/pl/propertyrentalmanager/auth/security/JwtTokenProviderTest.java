package pl.propertyrentalmanager.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtProperties jwtProperties;
    private Clock fixedClock;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setJwtSecretKey("super_secret_test_key_that_is_at_least_256_bits_long_1234567890!");
        jwtProperties.setJwtIssuer("property-rental-manager");
        jwtProperties.setJwtAudience("property-rental-manager-api");
        jwtProperties.setAccessTokenExpireMinutes(15);

        fixedClock = Clock.fixed(Instant.parse("2026-08-08T12:00:00Z"), ZoneId.of("UTC"));
        jwtTokenProvider = new JwtTokenProvider(jwtProperties, fixedClock);
    }

    @Test
    @DisplayName("Should generate valid JWT access token with correct claims")
    void shouldGenerateValidJwtAccessToken() {
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ADMIN", "OWNER");

        String token = jwtTokenProvider.generateAccessToken(userId, "admin@example.com", roles, 1);
        assertThat(token).isNotBlank();

        Claims claims = jwtTokenProvider.parseAndValidateToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email")).isEqualTo("admin@example.com");
        assertThat(claims.getIssuer()).isEqualTo("property-rental-manager");
        assertThat(claims.getAudience()).contains("property-rental-manager-api");
        assertThat(jwtTokenProvider.getAuthVersionFromClaims(claims)).isEqualTo(1);
        assertThat(jwtTokenProvider.getRolesFromClaims(claims)).containsExactly("ADMIN", "OWNER");
    }

    @Test
    @DisplayName("Should reject token when expired")
    void shouldRejectExpiredToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "user@example.com", List.of("TENANT"), 0);

        // Move clock forward by 20 minutes (exceeds 15 min TTL)
        Clock futureClock = Clock.fixed(Instant.parse("2026-08-08T12:20:00Z"), ZoneId.of("UTC"));
        JwtTokenProvider futureProvider = new JwtTokenProvider(jwtProperties, futureClock);

        assertThatThrownBy(() -> futureProvider.parseAndValidateToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should reject token signed with different secret key")
    void shouldRejectTokenWithWrongSignature() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "user@example.com", List.of("TENANT"), 0);

        JwtProperties badProperties = new JwtProperties();
        badProperties.setJwtSecretKey("another_different_super_secret_test_key_1234567890!");
        badProperties.setJwtIssuer("property-rental-manager");
        badProperties.setJwtAudience("property-rental-manager-api");

        JwtTokenProvider badProvider = new JwtTokenProvider(badProperties, fixedClock);

        assertThatThrownBy(() -> badProvider.parseAndValidateToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should reject secret key shorter than 256 bits")
    void shouldRejectShortSecretKey() {
        JwtProperties shortProperties = new JwtProperties();
        shortProperties.setJwtSecretKey("too_short_key");

        assertThatThrownBy(() -> new JwtTokenProvider(shortProperties, fixedClock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 256 bits");
    }
}
