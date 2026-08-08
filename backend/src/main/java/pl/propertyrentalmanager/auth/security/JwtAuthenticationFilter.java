package pl.propertyrentalmanager.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.propertyrentalmanager.user.RoleEntity;
import pl.propertyrentalmanager.user.UserEntity;
import pl.propertyrentalmanager.user.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                try {
                    Claims claims = jwtTokenProvider.parseAndValidateToken(token);
                    UUID userId = jwtTokenProvider.getUserIdFromClaims(claims);
                    int tokenAuthVersion = jwtTokenProvider.getAuthVersionFromClaims(claims);

                    Optional<UserEntity> userOpt = userRepository.findById(userId);

                    if (userOpt.isPresent()) {
                        UserEntity user = userOpt.get();

                        // Verify user is ACTIVE and authVersion matches
                        if ("ACTIVE".equals(user.getStatus()) && user.getAuthVersion() == tokenAuthVersion) {
                            Set<String> roleCodes = user.getRoles().stream()
                                    .map(RoleEntity::getCode)
                                    .collect(Collectors.toSet());

                            CurrentUser currentUser = new CurrentUser(
                                    user.getId(),
                                    user.getEmail(),
                                    user.getFullName(),
                                    user.getStatus(),
                                    user.getPreferredLocale(),
                                    roleCodes,
                                    user.getAuthVersion()
                            );

                            List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                                    .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                                    .collect(Collectors.toList());

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(currentUser, null, authorities);

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (JwtException e) {
                    // Invalid / expired token - SecurityContext remains unauthenticated
                    // Will be handled by RestAuthenticationEntryPoint if endpoint requires authentication
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Guarantee thread-local security context cleanup is handled by Spring Security,
            // but we can ensure clean state if needed.
        }
    }
}
