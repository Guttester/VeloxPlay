package gut.dev.veloxplay.Config;

import gut.dev.veloxplay.Enum.Role;
import gut.dev.veloxplay.Model.User;
import gut.dev.veloxplay.Repository.RUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
public class AccountBootstrap implements CommandLineRunner {
    private final RUser users;
    private final PasswordEncoder encoder;
    private final boolean seed;
    private final String moderatorEmail, moderatorPassword, demoEmail, demoPassword, adminEmail, adminPassword;

    public AccountBootstrap(RUser users, PasswordEncoder encoder,
            @Value("${app.seed.enabled:true}") boolean seed,
            @Value("${app.seed.moderator-email:moderador@veloxplay.com}") String moderatorEmail,
            @Value("${app.seed.moderator-password:Moderador@123}") String moderatorPassword,
            @Value("${app.seed.demo-email:demo@veloxplay.com}") String demoEmail,
            @Value("${app.seed.demo-password:Demo@123}") String demoPassword,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.users = users;
        this.encoder = encoder;
        this.seed = seed;
        this.moderatorEmail = moderatorEmail.trim().toLowerCase(java.util.Locale.ROOT);
        this.moderatorPassword = moderatorPassword;
        this.demoEmail = demoEmail.trim().toLowerCase(java.util.Locale.ROOT);
        this.demoPassword = demoPassword;
        this.adminEmail = adminEmail.trim().toLowerCase(java.util.Locale.ROOT);
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (moderatorEmail.isBlank() || demoEmail.isBlank() || moderatorEmail.equals(demoEmail)) {
            throw new IllegalStateException("Configure distinct public demo emails");
        }
        // The old publicly documented administrator must lose privileges, even with seeding disabled.
        users.findByEmailIgnoreCase("admin@veloxplay.com").ifPresent(legacy -> {
            legacy.setRole(Role.MODERATOR);
            legacy.setDemoAccount(true);
            if (!moderatorEmail.equalsIgnoreCase(legacy.getEmail())
                    && users.findByEmailIgnoreCase(moderatorEmail).isEmpty()) {
                legacy.setEmail(moderatorEmail);
                legacy.setNome(availableName("Moderador"));
                legacy.setSenha(encoder.encode(moderatorPassword));
            }
            users.saveAndFlush(legacy);
        });
        ensureDemo(moderatorEmail, moderatorPassword, "Moderador", Role.MODERATOR);
        ensureDemo(demoEmail, demoPassword, "Demo", Role.USER);

        if (adminEmail.isBlank() && adminPassword.isBlank()) return;
        if (adminEmail.isBlank() || adminPassword.length() < 12 || adminPassword.length() > 72
                || adminEmail.equals(moderatorEmail) || adminEmail.equals(demoEmail)
                || adminEmail.equals("admin@veloxplay.com")) {
            throw new IllegalStateException("Configure a private ADMIN_EMAIL and ADMIN_PASSWORD (12–72 characters)");
        }
        User existing = users.findByEmailIgnoreCase(adminEmail).orElse(null);
        if (existing != null) {
            if (existing.isDemoAccount() || existing.getRole() != Role.ADMIN) {
                throw new IllegalStateException("ADMIN_EMAIL belongs to a non-administrator account");
            }
            return; // Never overwrite an existing private password on restart.
        }
        users.save(account("Administrador privado", adminEmail, adminPassword, Role.ADMIN, false));
    }

    private void ensureDemo(String email, String password, String name, Role role) {
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            if (seed) users.save(account(name, email, password, role, true));
        } else {
            user.setRole(role);
            user.setDemoAccount(true);
            users.save(user);
        }
    }

    private User account(String name, String email, String password, Role role, boolean demo) {
        User user = new User();
        user.setNome(availableName(name));
        user.setEmail(email);
        user.setSenha(encoder.encode(password));
        user.setRole(role);
        user.setDemoAccount(demo);
        return user;
    }

    private String availableName(String preferred) {
        if (!users.existsByNomeIgnoreCase(preferred)) return preferred;
        return preferred + " " + java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}
