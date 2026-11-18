package gut.dev.veloxplay.Service;

import gut.dev.veloxplay.Enum.Role;
import gut.dev.veloxplay.Model.RefreshToken;
import gut.dev.veloxplay.Model.User;
import gut.dev.veloxplay.Repository.RRefreshToken;
import gut.dev.veloxplay.Repository.RUser;
import gut.dev.veloxplay.dto.AuthResponse;
import gut.dev.veloxplay.dto.LoginRequest;
import gut.dev.veloxplay.dto.RegisterRequest;
import gut.dev.veloxplay.dto.UserResponse;
import gut.dev.veloxplay.exception.ConflictException;
import gut.dev.veloxplay.exception.UnauthorizedException;
import gut.dev.veloxplay.security.JwtService;
import gut.dev.veloxplay.security.DemoAccountPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SAuth {
    private final RUser users;
    private final RRefreshToken refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DemoAccountPolicy accountPolicy;
    private final SecureRandom random = new SecureRandom();

    public SAuth(RUser users, RRefreshToken refreshTokens, PasswordEncoder passwordEncoder,
                 AuthenticationManager authenticationManager, JwtService jwtService, DemoAccountPolicy accountPolicy) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountPolicy = accountPolicy;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        accountPolicy.assertAvailableEmail(request.getEmail());
        if (users.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        if (users.existsByNomeIgnoreCase(request.getNome())) {
            throw new ConflictException("Nome de usuário já existe");
        }
        User user = new User();
        user.setNome(request.getNome());
        user.setEmail(request.getEmail().toLowerCase());
        user.setSenha(passwordEncoder.encode(request.getSenha()));
        user.setRole(Role.USER);
        users.save(user);
        return issue(user, true);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getSenha()));
        User user = users.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos"));
        return issue(user, true);
    }

    @Transactional
    public AuthResponse refresh(String rawToken) {
        String hash = hash(rawToken);
        RefreshToken stored = refreshTokens.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        if (stored.isRevoked() || stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokens.save(stored);
            throw new UnauthorizedException("Refresh token expirado");
        }
        stored.setRevoked(true);
        refreshTokens.save(stored);
        return issue(stored.getUser(), true);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokens.save(token);
        });
    }

    private AuthResponse issue(User user, boolean rotateRefresh) {
        String access = jwtService.generateAccessToken(user);
        String refresh = createRefreshToken(user);
        return new AuthResponse(access, refresh, jwtService.getAccessExpirationMs() / 1000, UserResponse.from(user));
    }

    private String createRefreshToken(User user) {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash(raw));
        entity.setUser(user);
        entity.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()));
        entity.setRevoked(false);
        refreshTokens.save(entity);
        return raw;
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
