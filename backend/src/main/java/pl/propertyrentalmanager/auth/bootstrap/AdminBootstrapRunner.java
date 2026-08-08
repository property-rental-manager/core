package pl.propertyrentalmanager.auth.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.propertyrentalmanager.auth.security.PasswordPolicyValidator;
import pl.propertyrentalmanager.user.RoleEntity;
import pl.propertyrentalmanager.user.RoleRepository;
import pl.propertyrentalmanager.user.UserEntity;
import pl.propertyrentalmanager.user.UserRepository;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final boolean enabled;
    private final String email;
    private final String password;
    private final String fullName;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public AdminBootstrapRunner(
            @Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.full-name:Administrator}") String fullName,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator
    ) {
        this.enabled = enabled;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Admin bootstrap is disabled.");
            return;
        }

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("Admin bootstrap is enabled but email or password is empty. Skipping admin creation.");
            return;
        }

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);
        passwordPolicyValidator.validatePassword(password);

        Optional<UserEntity> existingUserOpt = userRepository.findByEmailIgnoreCase(emailNormalized);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();
            boolean isAdmin = existingUser.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode()));

            if (isAdmin && "ACTIVE".equals(existingUser.getStatus())) {
                log.info("Admin account [{}] already exists and is active. Skipping bootstrap.", emailNormalized);
            } else {
                log.warn("Account [{}] exists but status is [{}] or missing ADMIN role. Skipping silent overwrite.",
                        emailNormalized, existingUser.getStatus());
            }
            return;
        }

        RoleEntity adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found in database. Ensure Flyway V1 migration ran successfully."));

        UserEntity adminUser = new UserEntity();
        adminUser.setEmail(emailNormalized);
        adminUser.setPasswordHash(passwordEncoder.encode(password));
        adminUser.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : "Administrator");
        adminUser.setStatus("ACTIVE");
        adminUser.setPreferredLocale("pl");
        adminUser.setAuthVersion(0);
        adminUser.getRoles().add(adminRole);

        userRepository.save(adminUser);
        log.info("Successfully bootstrapped initial admin user [{}] with ADMIN role.", emailNormalized);
    }
}
