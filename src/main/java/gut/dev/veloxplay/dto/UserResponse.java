package gut.dev.veloxplay.dto;

import gut.dev.veloxplay.Enum.Role;
import gut.dev.veloxplay.Model.User;

public class UserResponse {
    private Long id;
    private String nome;
    private String email;
    private Role role;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.nome = user.getNome();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
}
