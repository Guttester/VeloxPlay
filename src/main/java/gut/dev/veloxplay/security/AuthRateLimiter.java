package gut.dev.veloxplay.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** Bounded, per-instance limiter. Shared installations should also limit at the gateway. */
@Component
public class AuthRateLimiter {
    private record Window(long expiresAt, int attempts) {}
    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;
    private final int capacity;
    private int requests;

    @Autowired
    public AuthRateLimiter(@Value("${app.security.rate-limit.max-entries:10000}") int capacity) {
        this(Clock.systemUTC(), capacity);
    }

    AuthRateLimiter(Clock clock, int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Rate limiter capacity must be positive");
        this.clock = clock; this.capacity = capacity;
    }

    /** Returns zero if accepted, otherwise the number of seconds before retrying. */
    public synchronized long consume(String key, int limit, int seconds) {
        if (limit < 1 || seconds < 1) throw new IllegalArgumentException("Rate limits must be positive");
        long now = clock.millis();
        if (++requests % 128 == 0 || windows.size() >= capacity) windows.values().removeIf(window -> window.expiresAt <= now);
        Window window = windows.get(key);
        if (window == null || window.expiresAt <= now) {
            if (windows.size() >= capacity && window == null) return 60;
            windows.put(key, new Window(now + seconds * 1000L, 1));
            return 0;
        }
        if (window.attempts >= limit) return Math.max(1, (window.expiresAt - now + 999) / 1000);
        windows.put(key, new Window(window.expiresAt, window.attempts + 1));
        return 0;
    }
}
