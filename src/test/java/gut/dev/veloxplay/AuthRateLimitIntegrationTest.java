package gut.dev.veloxplay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = { "app.security.rate-limit.enabled=true", "app.security.rate-limit.login=2", "app.security.rate-limit.register=2" })
@AutoConfigureMockMvc
class AuthRateLimitIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void loginIsLimitedAndForgedForwardedHeadersCannotBypassIt() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/auth/login").with(request -> { request.setRemoteAddr("203.0.113.40"); return request; })
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"absent@example.invalid\",\"senha\":\"wrong123\"}"))
                .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/auth/login").with(request -> { request.setRemoteAddr("203.0.113.40"); return request; })
            .header("X-Forwarded-For", "198.51.100.1").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"absent@example.invalid\",\"senha\":\"wrong123\"}"))
            .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
        mvc.perform(get("/filmes").with(request -> { request.setRemoteAddr("203.0.113.40"); return request; }))
            .andExpect(status().isOk());
    }

    @Test void registrationIsLimitedEvenForInvalidBodies() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/auth/register").with(request -> { request.setRemoteAddr("203.0.113.41"); return request; })
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/auth/register").with(request -> { request.setRemoteAddr("203.0.113.41"); return request; })
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.status").value(429));
    }
}
