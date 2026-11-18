package gut.dev.veloxplay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

class AuthRateLimiterTest {
    private static class TestClock extends Clock {
        long millis;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
    }

    @Test void blocksAfterLimitAndResetsWhenWindowExpires() {
        TestClock clock = new TestClock();
        AuthRateLimiter limiter = new AuthRateLimiter(clock, 10);
        assertEquals(0, limiter.consume("login:one", 2, 60));
        assertEquals(0, limiter.consume("login:one", 2, 60));
        assertEquals(60, limiter.consume("login:one", 2, 60));
        assertEquals(0, limiter.consume("login:two", 2, 60));
        assertEquals(0, limiter.consume("register:one", 2, 60));
        clock.millis = 60000;
        assertEquals(0, limiter.consume("login:one", 2, 60));
    }

    @Test void keepsMemoryBoundedInsteadOfEvictingAnActiveLimit() {
        TestClock clock = new TestClock();
        AuthRateLimiter limiter = new AuthRateLimiter(clock, 1);
        assertEquals(0, limiter.consume("one", 1, 60));
        assertTrue(limiter.consume("two", 1, 60) > 0);
        assertTrue(limiter.consume("one", 1, 60) > 0);
        clock.millis = 60000;
        assertEquals(0, limiter.consume("two", 1, 60));
    }

    @Test void ignoresForwardedHeadersFromUntrustedClients() {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(new AuthRateLimiter(10), new ObjectMapper(), "10.0.0.1/32");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.5");
        request.addHeader("X-Forwarded-For", "198.51.100.9");
        assertEquals("203.0.113.5", filter.clientAddress(request));
    }

    @Test void walksProxyChainFromTheTrustedEnd() {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(new AuthRateLimiter(10), new ObjectMapper(), "10.0.0.1/32");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "198.51.100.9, 203.0.113.5");
        assertEquals("203.0.113.5", filter.clientAddress(request));
    }
}
