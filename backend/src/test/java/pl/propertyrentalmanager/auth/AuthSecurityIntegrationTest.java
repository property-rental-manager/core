package pl.propertyrentalmanager.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.propertyrentalmanager.TestcontainersConfiguration;
import pl.propertyrentalmanager.auth.entity.AuthenticationEventEntity;
import pl.propertyrentalmanager.auth.entity.RefreshTokenEntity;
import pl.propertyrentalmanager.auth.repository.AuthenticationEventRepository;
import pl.propertyrentalmanager.auth.repository.RefreshTokenRepository;
import pl.propertyrentalmanager.auth.security.JwtTokenProvider;
import pl.propertyrentalmanager.auth.security.LoginRateLimiter;
import pl.propertyrentalmanager.auth.web.dto.ChangePasswordRequest;
import pl.propertyrentalmanager.auth.web.dto.LoginRequest;
import pl.propertyrentalmanager.user.RoleEntity;
import pl.propertyrentalmanager.user.RoleRepository;
import pl.propertyrentalmanager.user.UserEntity;
import pl.propertyrentalmanager.user.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthenticationEventRepository authenticationEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    private UserEntity testUser;
    private final String rawPassword = "TestPassword123!";

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        authenticationEventRepository.deleteAll();
        userRepository.deleteAll();

        loginRateLimiter.resetRateLimit("user@example.com", "127.0.0.1");
        loginRateLimiter.resetRateLimit("disabled@example.com", "127.0.0.1");

        RoleEntity ownerRole = roleRepository.findByCode("OWNER")
                .orElseGet(() -> roleRepository.save(new RoleEntity(null, "OWNER", "Property Owner", "Owner description")));

        testUser = new UserEntity();
        testUser.setEmail("user@example.com");
        testUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        testUser.setFullName("Test User");
        testUser.setStatus("ACTIVE");
        testUser.setPreferredLocale("pl");
        testUser.setAuthVersion(0);
        testUser.setRoles(Set.of(ownerRole));

        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("1. Should log in successfully with valid credentials and return access token + HttpOnly refresh cookie")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(900)))
                .andExpect(jsonPath("$.user.email", is("user@example.com")))
                .andExpect(jsonPath("$.user.roles[0]", is("OWNER")))
                .andExpect(cookie().exists("prm_refresh_token"))
                .andExpect(cookie().httpOnly("prm_refresh_token", true))
                .andExpect(cookie().path("prm_refresh_token", "/api/v1/auth"));

        List<AuthenticationEventEntity> events = authenticationEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo("LOGIN_SUCCESS");
        assertThat(events.get(0).getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("2. Should reject login with invalid password and return generic INVALID_CREDENTIALS")
    void shouldRejectInvalidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "WrongPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));

        List<AuthenticationEventEntity> events = authenticationEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo("LOGIN_FAILURE");
        assertThat(events.get(0).getFailureReason()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("3. Should reject login for non-existent email with generic INVALID_CREDENTIALS")
    void shouldRejectNonExistentEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    @DisplayName("4. Should reject login for DISABLED user")
    void shouldRejectDisabledUser() throws Exception {
        testUser.setStatus("DISABLED");
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest("user@example.com", rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    @DisplayName("5. Should trigger rate limit 429 after 5 failed attempts")
    void shouldTriggerRateLimiter() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "WrongPassword123!");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt must be rate limited
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.code", is("RATE_LIMIT_EXCEEDED")));
    }

    @Test
    @DisplayName("6. Should fetch CSRF token from /api/v1/auth/csrf")
    void shouldFetchCsrfToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName", is("X-XSRF-TOKEN")))
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    @DisplayName("7. Should perform token refresh rotation when valid refresh cookie and CSRF header are provided")
    void shouldRotateRefreshTokenOnRefresh() throws Exception {
        // Step 1: Login to get initial refresh cookie
        LoginRequest loginRequest = new LoginRequest("user@example.com", rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("prm_refresh_token");
        assertThat(refreshCookie).isNotNull();

        // Step 2: Fetch CSRF token
        MvcResult csrfResult = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie xsrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        String csrfTokenValue = objectMapper.readTree(csrfResult.getResponse().getContentAsString()).get("token").asText();

        // Step 3: Perform refresh
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie, xsrfCookie)
                        .header("X-XSRF-TOKEN", csrfTokenValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(cookie().exists("prm_refresh_token"))
                .andReturn();

        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("prm_refresh_token");
        assertThat(newRefreshCookie).isNotNull();
        assertThat(newRefreshCookie.getValue()).isNotEqualTo(refreshCookie.getValue());
    }

    @Test
    @DisplayName("8. Should detect refresh token reuse, revoke family, bump authVersion, and return 401")
    void shouldDetectRefreshTokenReuse() throws Exception {
        // Step 1: Login
        LoginRequest loginRequest = new LoginRequest("user@example.com", rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie oldRefreshCookie = loginResult.getResponse().getCookie("prm_refresh_token");

        // Fetch CSRF
        MvcResult csrfResult = mockMvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie xsrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        String csrfTokenValue = objectMapper.readTree(csrfResult.getResponse().getContentAsString()).get("token").asText();

        // Step 2: First refresh (uses oldRefreshCookie, rotates it)
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(oldRefreshCookie, xsrfCookie)
                        .header("X-XSRF-TOKEN", csrfTokenValue))
                .andExpect(status().isOk());

        // Step 3: Attempt to REUSE oldRefreshCookie -> MUST BE REJECTED
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(oldRefreshCookie, xsrfCookie)
                        .header("X-XSRF-TOKEN", csrfTokenValue))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("REFRESH_TOKEN_REUSE_DETECTED")));

        // Verify user authVersion was incremented
        UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getAuthVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("9. Should return current user profile from /api/v1/me with valid Bearer token")
    void shouldGetMeWithValidToken() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("OWNER"), 0);

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.roles[0]", is("OWNER")));
    }

    @Test
    @DisplayName("10. Should reject /api/v1/me when Bearer token is missing")
    void shouldRejectMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_REQUIRED")));
    }

    @Test
    @DisplayName("11. Should reject /api/v1/me when Bearer token has outdated authVersion")
    void shouldRejectOutdatedAuthVersionToken() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("OWNER"), 0);

        // Increment authVersion in database
        testUser.setAuthVersion(1);
        userRepository.save(testUser);

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("12. Should change password, bump authVersion, and revoke all refresh tokens")
    void shouldChangePasswordSuccessfully() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("OWNER"), 0);
        ChangePasswordRequest request = new ChangePasswordRequest(rawPassword, "NewValidPassword123!");

        mockMvc.perform(post("/api/v1/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("prm_refresh_token", 0));

        UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewValidPassword123!", updatedUser.getPasswordHash())).isTrue();
        assertThat(updatedUser.getAuthVersion()).isEqualTo(1);
        assertThat(updatedUser.getPasswordChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("13. Should support CORS preflight OPTIONS request for allowed origin")
    void shouldSupportCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("14. Should reject public registration endpoint (4xx Client Error)")
    void shouldRejectPublicRegistrationEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }
}
