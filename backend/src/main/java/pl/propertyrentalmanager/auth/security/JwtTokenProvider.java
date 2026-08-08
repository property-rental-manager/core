package pl.propertyrentalmanager.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import pl.propertyrentalmanager.common.time.ClockConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;

        byte[] keyBytes = jwtProperties.getJwtSecretKey().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes) long");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId, String email, Collection<String> roles, int authVersion) {
        Instant now = clock.instant();
        Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenExpireMinutes() * 60L);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .claim("authVersion", authVersion)
                .issuer(jwtProperties.getJwtIssuer())
                .audience().add(jwtProperties.getJwtAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }

    public Claims parseAndValidateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.getJwtIssuer())
                .requireAudience(jwtProperties.getJwtAudience())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserIdFromClaims(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public int getAuthVersionFromClaims(Claims claims) {
        Integer authVersion = claims.get("authVersion", Integer.class);
        return authVersion != null ? authVersion : 0;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromClaims(Claims claims) {
        return claims.get("roles", List.class);
    }

    public int getAccessTokenExpireSeconds() {
        return jwtProperties.getAccessTokenExpireMinutes() * 60;
    }
}
