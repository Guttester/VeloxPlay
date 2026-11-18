package gut.dev.veloxplay.dto;

import gut.dev.veloxplay.Enum.Role;
import jakarta.validation.constraints.*;

// These records never become login accounts and never contain passwords.
public record DemoUserRequest(
        @NotBlank @Size(min = 2, max = 40) String nome,
        @NotBlank @Email @Size(max = 100) String email,
        @NotNull Role role) {}
