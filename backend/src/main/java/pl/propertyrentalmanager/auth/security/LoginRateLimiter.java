package pl.propertyrentalmanager.auth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class LoginRateLimiter {

    private final int maxAttempts;
    private final int windowMinutes;
    private final Clock clock;

    private final Cache<String, AttemptTracker> attemptsCache;

    public LoginRateLimiter(
            @Value("${app.auth.login-rate-limit-max-attempts:5}") int maxAttempts,
            @Value("${app.auth.login-rate-limit-window-minutes:15}") int windowMinutes,
            Clock clock
    ) {
        this.maxAttempts = maxAttempts;
        this.windowMinutes = windowMinutes;
        this.clock = clock;

        this.attemptsCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(windowMinutes, TimeUnit.MINUTES)
                .build();
    }

    public boolean isRateLimited(String emailNormalized, String ipAddress) {
        return checkKeyLimited(buildEmailKey(emailNormalized)) || checkKeyLimited(buildIpKey(ipAddress));
    }

    public long getRetryAfterSeconds(String emailNormalized, String ipAddress) {
        long emailSec = getRemainingLockSeconds(buildEmailKey(emailNormalized));
        long ipSec = getRemainingLockSeconds(buildIpKey(ipAddress));
        return Math.max(emailSec, ipSec);
    }

    public void recordFailedAttempt(String emailNormalized, String ipAddress) {
        Instant now = clock.instant();
        recordKeyAttempt(buildEmailKey(emailNormalized), now);
        recordKeyAttempt(buildIpKey(ipAddress), now);
    }

    public void resetRateLimit(String emailNormalized, String ipAddress) {
        if (emailNormalized != null) {
            attemptsCache.invalidate(buildEmailKey(emailNormalized));
        }
        if (ipAddress != null) {
            attemptsCache.invalidate(buildIpKey(ipAddress));
        }
    }

    private boolean checkKeyLimited(String key) {
        if (key == null) return false;
        AttemptTracker tracker = attemptsCache.getIfPresent(key);
        if (tracker == null) return false;
        return tracker.count >= maxAttempts;
    }

    private long getRemainingLockSeconds(String key) {
        if (key == null) return 0;
        AttemptTracker tracker = attemptsCache.getIfPresent(key);
        if (tracker == null || tracker.count < maxAttempts) return 0;
        Instant lockExpiry = tracker.firstAttemptTime.plusSeconds(windowMinutes * 60L);
        long remaining = lockExpiry.getEpochSecond() - clock.instant().getEpochSecond();
        return Math.max(remaining, 1);
    }

    private void recordKeyAttempt(String key, Instant now) {
        if (key == null) return;
        attemptsCache.asMap().compute(key, (k, existing) -> {
            if (existing == null) {
                return new AttemptTracker(1, now);
            }
            return new AttemptTracker(existing.count + 1, existing.firstAttemptTime);
        });
    }

    private String buildEmailKey(String emailNormalized) {
        return emailNormalized != null ? "email:" + emailNormalized.trim().toLowerCase() : null;
    }

    private String buildIpKey(String ipAddress) {
        return ipAddress != null ? "ip:" + ipAddress.trim() : null;
    }

    private record AttemptTracker(int count, Instant firstAttemptTime) {}
}
