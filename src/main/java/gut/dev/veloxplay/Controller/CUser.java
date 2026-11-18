package gut.dev.veloxplay.Controller;

import gut.dev.veloxplay.Service.SUser;
import gut.dev.veloxplay.dto.FilmeResponse;
import gut.dev.veloxplay.dto.UserResponse;
import gut.dev.veloxplay.dto.UserWriteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class CUser {
    private final SUser serviceUser;

    public CUser(SUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return serviceUser.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id, Authentication authentication) {
        serviceUser.assertSelfOrAdmin(id, authentication);
        return UserResponse.from(serviceUser.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserWriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(serviceUser.create(request)));
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserWriteRequest request,
                                   Authentication authentication) {
        serviceUser.assertSelfOrAdmin(id, authentication);
        return UserResponse.from(serviceUser.update(id, request, serviceUser.isAdmin(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        serviceUser.deleteById(id, serviceUser.currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/favoritos/{filmeId}")
    public List<FilmeResponse> adicionarFavorito(@PathVariable Long userId, @PathVariable Long filmeId,
                                                 Authentication authentication) {
        serviceUser.assertSelfOrAdmin(userId, authentication);
        return FilmeResponse.list(serviceUser.adicionarFavorito(userId, filmeId));
    }

    @DeleteMapping("/{userId}/favoritos/{filmeId}")
    public List<FilmeResponse> removerFavorito(@PathVariable Long userId, @PathVariable Long filmeId,
                                               Authentication authentication) {
        serviceUser.assertSelfOrAdmin(userId, authentication);
        return FilmeResponse.list(serviceUser.removerFavorito(userId, filmeId));
    }

    @GetMapping("/{userId}/favoritos")
    public List<FilmeResponse> getFavoritos(@PathVariable Long userId, Authentication authentication) {
        serviceUser.assertSelfOrAdmin(userId, authentication);
        return FilmeResponse.list(serviceUser.getFavoritos(userId));
    }
}
