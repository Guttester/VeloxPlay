package gut.dev.veloxplay.Service;

import gut.dev.veloxplay.Enum.Role;
import gut.dev.veloxplay.Model.Filme;
import gut.dev.veloxplay.Model.User;
import gut.dev.veloxplay.Repository.RFilme;
import gut.dev.veloxplay.Repository.RUser;
import gut.dev.veloxplay.Repository.RRefreshToken;
import gut.dev.veloxplay.security.DemoAccountPolicy;
import org.springframework.security.access.prepost.PreAuthorize;
import gut.dev.veloxplay.dto.UserWriteRequest;
import gut.dev.veloxplay.exception.ApiException;
import gut.dev.veloxplay.exception.ConflictException;
import gut.dev.veloxplay.exception.ResourceNotFoundException;
import gut.dev.veloxplay.security.AppUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SUser {
    private final RUser repositoryUser;
    private final RFilme repositoryFilme;
    private final PasswordEncoder passwordEncoder;
    private final RRefreshToken refreshTokens;
    private final DemoAccountPolicy accountPolicy;

    public SUser(RUser repositoryUser, RFilme repositoryFilme, PasswordEncoder passwordEncoder,
                 RRefreshToken refreshTokens, DemoAccountPolicy accountPolicy) {
        this.repositoryUser = repositoryUser;
        this.repositoryFilme = repositoryFilme;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokens = refreshTokens;
        this.accountPolicy = accountPolicy;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repositoryUser.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repositoryUser.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User create(UserWriteRequest request) {
        accountPolicy.assertAvailableEmail(request.getEmail());
        assertUnique(request.getEmail(), request.getNome(), null);
        User user = new User();
        user.setNome(request.getNome());
        user.setEmail(request.getEmail().toLowerCase());
        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Senha é obrigatória");
        }
        user.setSenha(passwordEncoder.encode(request.getSenha()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        return repositoryUser.save(user);
    }

    @Transactional
    public User update(Long id, UserWriteRequest request, boolean admin) {
        User existing = findById(id);
        accountPolicy.assertMutable(existing);
        if (!request.getEmail().equalsIgnoreCase(existing.getEmail())) {
            accountPolicy.assertAvailableEmail(request.getEmail());
        }
        if (!admin && request.getRole() != null && request.getRole() != existing.getRole()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Você não pode alterar o próprio perfil de acesso");
        }
        assertUnique(request.getEmail(), request.getNome(), id);
        existing.setNome(request.getNome());
        existing.setEmail(request.getEmail().toLowerCase());
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            existing.setSenha(passwordEncoder.encode(request.getSenha()));
        }
        if (admin && request.getRole() != null) {
            existing.setRole(request.getRole());
        }
        return repositoryUser.save(existing);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id, Long requesterId) {
        if (id.equals(requesterId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Você não pode excluir a própria conta por aqui");
        }
        User existing = findById(id);
        accountPolicy.assertMutable(existing);
        refreshTokens.deleteByUser(existing);
        repositoryUser.delete(existing);
    }

    @Transactional
    public List<Filme> adicionarFavorito(Long userId, Long filmeId) {
        User user = findById(userId);
        Filme filme = repositoryFilme.findById(filmeId)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
        user.adicionarFavorito(filme);
        repositoryUser.save(user);
        return new ArrayList<>(user.getFavoritos());
    }

    @Transactional
    public List<Filme> removerFavorito(Long userId, Long filmeId) {
        User user = findById(userId);
        Filme filme = repositoryFilme.findById(filmeId)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
        user.removerFavorito(filme);
        repositoryUser.save(user);
        return new ArrayList<>(user.getFavoritos());
    }

    @Transactional(readOnly = true)
    public List<Filme> getFavoritos(Long userId) {
        return new ArrayList<>(findById(userId).getFavoritos());
    }

    public void assertSelfOrAdmin(Long targetUserId, Authentication authentication) {
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        boolean admin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !principal.getId().equals(targetUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado");
        }
    }

    public Long currentUserId(Authentication authentication) {
        return ((AppUserDetails) authentication.getPrincipal()).getId();
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void assertUnique(String email, String nome, Long ignoreId) {
        repositoryUser.findByEmailIgnoreCase(email)
                .filter(u -> ignoreId == null || !u.getId().equals(ignoreId))
                .ifPresent(u -> { throw new ConflictException("E-mail já cadastrado"); });
        if (repositoryUser.existsByNomeIgnoreCase(nome)) {
            repositoryUser.findAll().stream()
                    .filter(u -> u.getNome().equalsIgnoreCase(nome))
                    .filter(u -> ignoreId == null || !u.getId().equals(ignoreId))
                    .findAny()
                    .ifPresent(u -> { throw new ConflictException("Nome de usuário já existe"); });
        }
    }
}
