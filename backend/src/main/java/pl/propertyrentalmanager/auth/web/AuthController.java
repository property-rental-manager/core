package pl.propertyrentalmanager.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.propertyrentalmanager.auth.service.AuthService;
import pl.propertyrentalmanager.auth.web.dto.CsrfTokenResponse;
import pl.propertyrentalmanager.auth.web.dto.LoginRequest;
import pl.propertyrentalmanager.auth.web.dto.LoginResponse;
import pl.propertyrentalmanager.common.web.RequestIdFilter;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final String cookieName;

    public AuthController(AuthService authService, @Value("${app.auth.refresh-cookie-name:prm_refresh_token}") String cookieName) {
        this.authService = authService;
        this.cookieName = cookieName;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String requestId = (String) httpRequest.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);

        LoginResponse response = authService.login(request, ipAddress, userAgent, requestId, httpResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfTokenResponse> getCsrfToken(CsrfToken csrfToken) {
        if (csrfToken == null) {
            return ResponseEntity.ok(new CsrfTokenResponse("X-XSRF-TOKEN", "_csrf", ""));
        }
        return ResponseEntity.ok(new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "prm_refresh_token", required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String requestId = (String) httpRequest.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);

        LoginResponse response = authService.refresh(refreshToken, ipAddress, userAgent, requestId, httpResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "prm_refresh_token", required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String requestId = (String) httpRequest.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);

        authService.logout(refreshToken, ipAddress, userAgent, requestId, httpResponse);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
