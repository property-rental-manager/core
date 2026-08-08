package pl.propertyrentalmanager.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.propertyrentalmanager.auth.entity.AuthenticationEventEntity;
import pl.propertyrentalmanager.auth.repository.AuthenticationEventRepository;
import pl.propertyrentalmanager.auth.repository.RefreshTokenRepository;
import pl.propertyrentalmanager.user.UserEntity;
import pl.propertyrentalmanager.user.UserRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthenticationEventService {

    private final AuthenticationEventRepository authenticationEventRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AuthenticationEventService(
            AuthenticationEventRepository authenticationEventRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.authenticationEventRepository = authenticationEventRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuthEvent(UserEntity user, String email, String eventType, String status, String failureReason, String ip, String userAgent, String requestId) {
        AuthenticationEventEntity event = new AuthenticationEventEntity();
        event.setUser(user);
        event.setEmailNormalized(email != null ? email.trim().toLowerCase() : null);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setFailureReason(failureReason);
        event.setIpAddress(ip);
        event.setUserAgent(userAgent);
        event.setRequestId(requestId);
        event.setCreatedAt(OffsetDateTime.now(clock));
        authenticationEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReuseDetected(UUID userId, UUID tokenFamilyId, String ipAddress, String userAgent, String requestId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        refreshTokenRepository.revokeFamily(tokenFamilyId, now, "REUSE_DETECTED");
        UserEntity user = userRepository.findById(userId).orElseThrow();
        user.setAuthVersion(user.getAuthVersion() + 1);
        userRepository.save(user);

        recordAuthEvent(user, user.getEmail(), "REFRESH_FAILURE", "REJECTED", "REUSE_DETECTED", ipAddress, userAgent, requestId);
    }
}
