package gut.dev.veloxplay.dto;

import gut.dev.veloxplay.Enum.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserWriteRequest {
    @NotBlank
    @Size(min = 2, max = 40)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Size(min = 8, max = 72)
    private String senha;

    private Role role;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
