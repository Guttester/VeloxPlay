package gut.dev.veloxplay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import gut.dev.veloxplay.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private final AuthRateLimiter limiter;
    private final ObjectMapper mapper;
    private final List<IpAddressMatcher> trusted;
    @Value("${app.security.rate-limit.enabled:true}") private boolean enabled;
    @Value("${app.security.rate-limit.login:20}") private int login;
    @Value("${app.security.rate-limit.register:5}") private int register;
    @Value("${app.security.rate-limit.refresh:60}") private int refresh;

    public AuthRateLimitFilter(AuthRateLimiter limiter, ObjectMapper mapper,
            @Value("${app.security.trusted-proxies:}") String trustedProxies) {
        this.limiter = limiter; this.mapper = mapper;
        this.trusted = Arrays.stream(trustedProxies.split(",")).map(String::trim).filter(value -> !value.isEmpty()).map(IpAddressMatcher::new).toList();
    }

    private boolean isTrusted(String address) { return trusted.stream().anyMatch(proxy -> proxy.matches(address)); }

    String clientAddress(HttpServletRequest request) {
        String current = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (!isTrusted(current) || forwarded == null || forwarded.length() > 2048) return current;
        String[] chain = forwarded.split(",");
        for (int i = chain.length - 1; i >= 0 && isTrusted(current); i--) {
            String candidate = chain[i].trim();
            // Accept literal IPs only. Never resolve a hostname from a header.
            if (!candidate.matches("[0-9a-fA-F:.]+")) break;
            try { new IpAddressMatcher(candidate); current = candidate; } catch (IllegalArgumentException ignored) { break; }
        }
        return current;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        int limit = switch (path) { case "/auth/login" -> login; case "/auth/register" -> register; case "/auth/refresh" -> refresh; default -> 0; };
        if (!enabled || !"POST".equals(request.getMethod()) || limit == 0) { chain.doFilter(request, response); return; }
        long retry = limiter.consume(path + ":" + clientAddress(request), limit, path.equals("/auth/register") ? 3600 : 60);
        if (retry == 0) { chain.doFilter(request, response); return; }
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retry));
        response.setHeader("Cache-Control", "no-store");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), new ApiError(429, "TOO_MANY_REQUESTS", "Muitas tentativas. Aguarde e tente novamente.", path));
    }
}
