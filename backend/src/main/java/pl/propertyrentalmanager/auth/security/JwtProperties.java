package pl.propertyrentalmanager.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.auth")
@Getter
@Setter
public class JwtProperties {

    private String jwtSecretKey = "default_development_secret_key_that_is_at_least_256_bits_long_for_hmac_sha256!";
    private String jwtIssuer = "property-rental-manager";
    private String jwtAudience = "property-rental-manager-api";
    private int accessTokenExpireMinutes = 15;
    private int refreshTokenExpireDays = 30;
    private String refreshCookieName = "prm_refresh_token";
    private boolean refreshCookieSecure = false;
    private String refreshCookieSameSite = "Lax";
    private String refreshCookiePath = "/api/v1/auth";
}
