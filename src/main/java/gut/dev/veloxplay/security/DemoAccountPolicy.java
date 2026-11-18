package gut.dev.veloxplay.security;

import gut.dev.veloxplay.Model.User;
import gut.dev.veloxplay.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoAccountPolicy {
    private final List<String> reservedEmails;

    public DemoAccountPolicy(
            @Value("${app.seed.moderator-email:moderador@veloxplay.com}") String moderator,
            @Value("${app.seed.demo-email:demo@veloxplay.com}") String demo,
            @Value("${app.admin.email:}") String admin) {
        reservedEmails = List.of("admin@veloxplay.com", moderator.trim(), demo.trim(), admin.trim());
    }

    public void assertAvailableEmail(String email) {
        if (reservedEmails.stream().anyMatch(reserved -> reserved.equalsIgnoreCase(email.trim()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "RESERVED_ACCOUNT", "E-mail reservado para uma conta do sistema");
        }
    }

    public void assertMutable(User user) {
        if (user.isDemoAccount()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "DEMO_ACCOUNT_PROTECTED",
                    "Contas demonstrativas não podem ser alteradas ou excluídas");
        }
    }
}
