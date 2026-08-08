package pl.propertyrentalmanager.auth.service;

import lombok.Getter;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.propertyrentalmanager.auth.entity.RefreshTokenEntity;
import pl.propertyrentalmanager.auth.repository.RefreshTokenRepository;
import pl.propertyrentalmanager.auth.security.JwtProperties;
import pl.propertyrentalmanager.common.error.ErrorCode;
import pl.propertyrentalmanager.user.UserEntity;
import pl.propertyrentalmanager.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthenticationEventService authenticationEventService;
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            AuthenticationEventService authenticationEventService,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authenticationEventService = authenticationEventService;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String generateRawRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional
    public RefreshTokenEntity issueRefreshToken(UserEntity user, String rawRefreshToken, UUID familyId, String ipAddress, String userAgent, String requestId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime expiresAt = now.plusDays(jwtProperties.getRefreshTokenExpireDays());

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(hashToken(rawRefreshToken));
        entity.setTokenFamilyId(familyId != null ? familyId : UUID.randomUUID());
        entity.setExpiresAt(expiresAt);
        entity.setCreatedIp(ipAddress);
        entity.setUserAgent(userAgent);
        entity.setRequestId(requestId);

        return refreshTokenRepository.save(entity);
    }

    @Transactional
    public TokenRotationResult rotateRefreshToken(String rawRefreshToken, String ipAddress, String userAgent, String requestId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        String tokenHash = hashToken(rawRefreshToken);

        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHashWithLock(tokenHash);

        if (tokenOpt.isEmpty()) {
            authenticationEventService.recordAuthEvent(null, null, "REFRESH_FAILURE", "REJECTED", "TOKEN_NOT_FOUND", ipAddress, userAgent, requestId);
            throw new TokenException(ErrorCode.REFRESH_TOKEN_INVALID, "Invalid refresh token");
        }

        RefreshTokenEntity token = tokenOpt.get();
        UserEntity user = token.getUser();

        // Check if token was already revoked (Reuse Detection)
        if (token.getRevokedAt() != null) {
            authenticationEventService.handleReuseDetected(user.getId(), token.getTokenFamilyId(), ipAddress, userAgent, requestId);
            throw new TokenException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED, "Refresh token reuse detected. All sessions revoked.");
        }

        // Check if expired
        if (token.getExpiresAt().isBefore(now)) {
            token.setRevokedAt(now);
            token.setRevokeReason("EXPIRED");
            refreshTokenRepository.save(token);

            authenticationEventService.recordAuthEvent(user, user.getEmail(), "REFRESH_FAILURE", "REJECTED", "TOKEN_EXPIRED", ipAddress, userAgent, requestId);
            throw new TokenException(ErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token expired");
        }

        // Check user active status
        if (!"ACTIVE".equals(user.getStatus())) {
            token.setRevokedAt(now);
            token.setRevokeReason("ACCOUNT_DISABLED");
            refreshTokenRepository.save(token);

            authenticationEventService.recordAuthEvent(user, user.getEmail(), "REFRESH_FAILURE", "REJECTED", "ACCOUNT_DISABLED", ipAddress, userAgent, requestId);
            throw new TokenException(ErrorCode.ACCOUNT_DISABLED, "User account is not active");
        }

        // Generate new refresh token in same family
        String newRawToken = generateRawRefreshToken();
        RefreshTokenEntity newToken = issueRefreshToken(user, newRawToken, token.getTokenFamilyId(), ipAddress, userAgent, requestId);

        // Mark current token as revoked/used and point to replaced_by
        token.setRevokedAt(now);
        token.setRevokeReason("ROTATED");
        token.setLastUsedAt(now);
        token.setReplacedByToken(newToken);
        refreshTokenRepository.save(token);

        authenticationEventService.recordAuthEvent(user, user.getEmail(), "REFRESH_SUCCESS", "SUCCESS", null, ipAddress, userAgent, requestId);

        return new TokenRotationResult(user, newRawToken);
    }



    @Transactional
    public void revokeRefreshToken(String rawRefreshToken, String ipAddress, String userAgent, String requestId) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        String tokenHash = hashToken(rawRefreshToken);
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isPresent()) {
            RefreshTokenEntity token = tokenOpt.get();
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(now);
                token.setRevokeReason("LOGOUT");
                refreshTokenRepository.save(token);
            }
            authenticationEventService.recordAuthEvent(token.getUser(), token.getUser().getEmail(), "LOGOUT", "SUCCESS", null, ipAddress, userAgent, requestId);
        }
    }

    @Transactional
    public void revokeAllUserTokens(UserEntity user, String reason) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        refreshTokenRepository.revokeAllByUserId(user.getId(), now, reason);
    }

    public ResponseCookie createRefreshCookie(String rawRefreshToken, int expireDays) {
        long maxAgeSeconds = expireDays * 86400L;
        return ResponseCookie.from(jwtProperties.getRefreshCookieName(), rawRefreshToken)
                .httpOnly(true)
                .secure(jwtProperties.isRefreshCookieSecure())
                .path(jwtProperties.getRefreshCookiePath())
                .sameSite(jwtProperties.getRefreshCookieSameSite())
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie createCleanRefreshCookie() {
        return ResponseCookie.from(jwtProperties.getRefreshCookieName(), "")
                .httpOnly(true)
                .secure(jwtProperties.isRefreshCookieSecure())
                .path(jwtProperties.getRefreshCookiePath())
                .sameSite(jwtProperties.getRefreshCookieSameSite())
                .maxAge(0)
                .build();
    }

    public record TokenRotationResult(UserEntity user, String newRawRefreshToken) {}

    @Getter
    public static class TokenException extends RuntimeException {
        private final ErrorCode errorCode;

        public TokenException(ErrorCode errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
    }
}
