package pl.propertyrentalmanager.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.propertyrentalmanager.auth.security.CurrentUser;
import pl.propertyrentalmanager.auth.security.JwtProperties;
import pl.propertyrentalmanager.auth.security.JwtTokenProvider;
import pl.propertyrentalmanager.auth.security.LoginRateLimiter;
import pl.propertyrentalmanager.auth.security.PasswordPolicyValidator;
import pl.propertyrentalmanager.auth.web.dto.ChangePasswordRequest;
import pl.propertyrentalmanager.auth.web.dto.LoginRequest;
import pl.propertyrentalmanager.auth.web.dto.LoginResponse;
import pl.propertyrentalmanager.auth.web.dto.UserSummaryDto;
import pl.propertyrentalmanager.common.error.ErrorCode;
import pl.propertyrentalmanager.common.exception.RateLimitExceededException;
import pl.propertyrentalmanager.common.exception.ResourceNotFoundException;
import pl.propertyrentalmanager.user.RoleEntity;
import pl.propertyrentalmanager.user.UserEntity;
import pl.propertyrentalmanager.user.UserRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationEventService authenticationEventService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final LoginRateLimiter loginRateLimiter;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            AuthenticationEventService authenticationEventService,
            RefreshTokenService refreshTokenService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator,
            LoginRateLimiter loginRateLimiter,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.authenticationEventService = authenticationEventService;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.loginRateLimiter = loginRateLimiter;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent, String requestId, HttpServletResponse response) {
        String emailNormalized = request.getEmail().trim().toLowerCase(Locale.ROOT);

        // Rate limiting check
        if (loginRateLimiter.isRateLimited(emailNormalized, ipAddress)) {
            long retryAfterSec = loginRateLimiter.getRetryAfterSeconds(emailNormalized, ipAddress);
            authenticationEventService.recordAuthEvent(null, emailNormalized, "LOGIN_FAILURE", "REJECTED", "RATE_LIMIT_EXCEEDED", ipAddress, userAgent, requestId);
            throw new RateLimitExceededException("Too many failed login attempts. Please try again later.", retryAfterSec);
        }

        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(emailNormalized);

        if (userOpt.isEmpty()) {
            loginRateLimiter.recordFailedAttempt(emailNormalized, ipAddress);
            authenticationEventService.recordAuthEvent(null, emailNormalized, "LOGIN_FAILURE", "FAILURE", "INVALID_CREDENTIALS", ipAddress, userAgent, requestId);
            throw new RefreshTokenService.TokenException(ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_CREDENTIALS.getDefaultMessage());
        }

        UserEntity user = userOpt.get();

        if (!"ACTIVE".equals(user.getStatus())) {
            loginRateLimiter.recordFailedAttempt(emailNormalized, ipAddress);
            authenticationEventService.recordAuthEvent(user, emailNormalized, "LOGIN_FAILURE", "REJECTED", "ACCOUNT_DISABLED", ipAddress, userAgent, requestId);
            throw new RefreshTokenService.TokenException(ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_CREDENTIALS.getDefaultMessage());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailedAttempt(emailNormalized, ipAddress);
            authenticationEventService.recordAuthEvent(user, emailNormalized, "LOGIN_FAILURE", "FAILURE", "INVALID_CREDENTIALS", ipAddress, userAgent, requestId);
            throw new RefreshTokenService.TokenException(ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_CREDENTIALS.getDefaultMessage());
        }

        // Login successful
        loginRateLimiter.resetRateLimit(emailNormalized, ipAddress);
        user.setLastLoginAt(OffsetDateTime.now(clock));
        userRepository.save(user);

        Set<String> roleCodes = user.getRoles().stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roleCodes, user.getAuthVersion());
        String rawRefreshToken = refreshTokenService.generateRawRefreshToken();

        refreshTokenService.issueRefreshToken(user, rawRefreshToken, null, ipAddress, userAgent, requestId);

        ResponseCookie refreshCookie = refreshTokenService.createRefreshCookie(rawRefreshToken, jwtProperties.getRefreshTokenExpireDays());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        authenticationEventService.recordAuthEvent(user, emailNormalized, "LOGIN_SUCCESS", "SUCCESS", null, ipAddress, userAgent, requestId);

        UserSummaryDto userSummary = new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getPreferredLocale(),
                roleCodes
        );

        return new LoginResponse(accessToken, "Bearer", jwtTokenProvider.getAccessTokenExpireSeconds(), userSummary);
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken, String ipAddress, String userAgent, String requestId, HttpServletResponse response) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            authenticationEventService.recordAuthEvent(null, null, "REFRESH_FAILURE", "REJECTED", "MISSING_REFRESH_TOKEN", ipAddress, userAgent, requestId);
            ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshCookie();
            response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
            throw new RefreshTokenService.TokenException(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh token is missing");
        }

        RefreshTokenService.TokenRotationResult rotationResult;
        try {
            rotationResult = refreshTokenService.rotateRefreshToken(rawRefreshToken, ipAddress, userAgent, requestId);
        } catch (RefreshTokenService.TokenException e) {
            ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshCookie();
            response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
            throw e;
        }

        UserEntity user = rotationResult.user();
        Set<String> roleCodes = user.getRoles().stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toSet());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roleCodes, user.getAuthVersion());
        ResponseCookie newCookie = refreshTokenService.createRefreshCookie(rotationResult.newRawRefreshToken(), jwtProperties.getRefreshTokenExpireDays());
        response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

        UserSummaryDto userSummary = new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getPreferredLocale(),
                roleCodes
        );

        return new LoginResponse(newAccessToken, "Bearer", jwtTokenProvider.getAccessTokenExpireSeconds(), userSummary);
    }

    @Transactional
    public void logout(String rawRefreshToken, String ipAddress, String userAgent, String requestId, HttpServletResponse response) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(rawRefreshToken, ipAddress, userAgent, requestId);
        }

        ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUserSummary(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new RefreshTokenService.TokenException(ErrorCode.AUTHENTICATION_REQUIRED, ErrorCode.AUTHENTICATION_REQUIRED.getDefaultMessage());
        }

        UserEntity user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.userId()));

        Set<String> roleCodes = user.getRoles().stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toSet());

        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getPreferredLocale(),
                roleCodes
        );
    }

    @Transactional
    public void changePassword(CurrentUser currentUser, ChangePasswordRequest request, String rawRefreshToken, String ipAddress, String userAgent, String requestId, HttpServletResponse response) {
        if (currentUser == null) {
            throw new RefreshTokenService.TokenException(ErrorCode.AUTHENTICATION_REQUIRED, ErrorCode.AUTHENTICATION_REQUIRED.getDefaultMessage());
        }

        UserEntity user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.userId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            authenticationEventService.recordAuthEvent(user, user.getEmail(), "PASSWORD_CHANGED", "FAILURE", "CURRENT_PASSWORD_INVALID", ipAddress, userAgent, requestId);
            throw new IllegalArgumentException("The current password provided is incorrect");
        }

        passwordPolicyValidator.validatePasswordChange(request.getCurrentPassword(), request.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(OffsetDateTime.now(clock));
        user.setAuthVersion(user.getAuthVersion() + 1);
        userRepository.save(user);

        // Revoke all refresh tokens for user and clear cookie
        refreshTokenService.revokeAllUserTokens(user, "PASSWORD_CHANGED");
        ResponseCookie cleanCookie = refreshTokenService.createCleanRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());

        authenticationEventService.recordAuthEvent(user, user.getEmail(), "PASSWORD_CHANGED", "SUCCESS", null, ipAddress, userAgent, requestId);
    }
}
