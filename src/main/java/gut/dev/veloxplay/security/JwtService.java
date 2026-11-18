package gut.dev.veloxplay.security;

import gut.dev.veloxplay.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            throw new IllegalStateException("JWT_SECRET não configurado. Defina a variável de ambiente ou JWT_SECRET no arquivo .env da pasta do backend");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 bytes. Configure uma chave aleatória privada no .env ou na variável de ambiente");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(properties.getAccessExpirationMs());
        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isExpired(String token) {
        try {
            parse(token);
            return false;
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    public long getAccessExpirationMs() {
        return properties.getAccessExpirationMs();
    }

    public long getRefreshExpirationMs() {
        return properties.getRefreshExpirationMs();
    }
}
