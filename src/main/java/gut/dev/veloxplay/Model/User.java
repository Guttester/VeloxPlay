package gut.dev.veloxplay.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gut.dev.veloxplay.Enum.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 120)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'USER'")
    private Role role = Role.USER;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean demoAccount = false;

    @ManyToMany
    @JoinTable(
            name = "user_favoritos",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "filme_id")
    )
    private List<Filme> favoritos = new ArrayList<>();

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonIgnore
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isDemoAccount() { return demoAccount; }
    public void setDemoAccount(boolean demoAccount) { this.demoAccount = demoAccount; }

    public List<Filme> getFavoritos() { return favoritos; }
    public void setFavoritos(List<Filme> favoritos) { this.favoritos = favoritos; }

    public void adicionarFavorito(Filme filme) {
        if (filme == null) return;
        boolean exists = favoritos.stream().anyMatch(f -> Objects.equals(f.getId(), filme.getId()));
        if (!exists) {
            favoritos.add(filme);
        }
    }

    public void removerFavorito(Filme filme) {
        if (filme == null) return;
        favoritos.removeIf(f -> Objects.equals(f.getId(), filme.getId()));
    }
}
