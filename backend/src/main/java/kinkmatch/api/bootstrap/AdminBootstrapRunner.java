package kinkmatch.api.bootstrap;

import kinkmatch.api.model.Role;
import kinkmatch.api.model.User;
import kinkmatch.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_BOOTSTRAP_ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()) return;
        if (adminPassword == null || adminPassword.isBlank()) return;

        var existingOptional = userRepository.findByEmail(adminEmail);

        if (existingOptional.isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            System.out.println("[BOOTSTRAP] Admin created: " + adminEmail);
            return;
        }

        User existing = existingOptional.get();

        if (existing.getRole() != Role.ADMIN) {
            existing.setRole(Role.ADMIN);
            userRepository.save(existing);
            System.out.println("[BOOTSTRAP] User promoted to ADMIN: " + adminEmail);
        } else {
            System.out.println("[BOOTSTRAP] Admin already exists: " + adminEmail);
        }
    }
}
