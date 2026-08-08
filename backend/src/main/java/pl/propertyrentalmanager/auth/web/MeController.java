package pl.propertyrentalmanager.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.propertyrentalmanager.auth.security.CurrentUser;
import pl.propertyrentalmanager.auth.service.AuthService;
import pl.propertyrentalmanager.auth.web.dto.ChangePasswordRequest;
import pl.propertyrentalmanager.auth.web.dto.UserSummaryDto;
import pl.propertyrentalmanager.common.web.RequestIdFilter;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AuthService authService;

    public MeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<UserSummaryDto> getCurrentUser(@AuthenticationPrincipal CurrentUser currentUser) {
        UserSummaryDto userSummary = authService.getCurrentUserSummary(currentUser);
        return ResponseEntity.ok(userSummary);
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request,
            @CookieValue(name = "prm_refresh_token", required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String requestId = (String) httpRequest.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);

        authService.changePassword(currentUser, request, refreshToken, ipAddress, userAgent, requestId, httpResponse);
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
