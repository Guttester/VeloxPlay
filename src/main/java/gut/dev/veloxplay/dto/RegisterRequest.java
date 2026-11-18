package gut.dev.veloxplay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Informe o nome")
    @Size(min = 2, max = 40, message = "Nome deve ter entre 2 e 40 caracteres")
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Informe a senha")
    @Size(min = 8, max = 72, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Senha deve conter letras e números")
    private String senha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
