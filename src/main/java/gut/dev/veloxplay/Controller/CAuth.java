package gut.dev.veloxplay.Controller;

import gut.dev.veloxplay.dto.AuthResponse;
import gut.dev.veloxplay.dto.LoginRequest;
import gut.dev.veloxplay.dto.LogoutRequest;
import gut.dev.veloxplay.dto.RefreshRequest;
import gut.dev.veloxplay.dto.RegisterRequest;
import gut.dev.veloxplay.dto.UserResponse;
import gut.dev.veloxplay.security.AppUserDetails;
import gut.dev.veloxplay.Service.SAuth;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class CAuth {
    private final SAuth auth;

    public CAuth(SAuth auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return auth.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        if (request != null) {
            auth.logout(request.getRefreshToken());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return UserResponse.from(principal.getUser());
    }
}
