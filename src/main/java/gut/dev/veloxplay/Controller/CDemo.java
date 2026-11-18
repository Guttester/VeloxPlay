package gut.dev.veloxplay.Controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gut.dev.veloxplay.Service.SDemo;
import gut.dev.veloxplay.dto.AdminStatsResponse;
import gut.dev.veloxplay.dto.DemoUserRequest;
import gut.dev.veloxplay.dto.FilmeRequest;
import gut.dev.veloxplay.security.AppUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class CDemo {
    private final SDemo demo;
    public CDemo(SDemo demo) { this.demo = demo; }

    @GetMapping("/stats")
    public AdminStatsResponse stats(@AuthenticationPrincipal AppUserDetails principal) {
        return demo.stats(principal);
    }

    @DeleteMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@AuthenticationPrincipal AppUserDetails principal) { demo.reset(principal); }

    @GetMapping("/filmes")
    public List<ObjectNode> filmes(@AuthenticationPrincipal AppUserDetails principal) { return demo.list(principal, "FILME"); }

    @GetMapping("/filmes/{id}")
    public ObjectNode filme(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) { return demo.get(principal, "FILME", id); }

    @PostMapping("/filmes")
    @ResponseStatus(HttpStatus.CREATED)
    public ObjectNode createFilme(@AuthenticationPrincipal AppUserDetails principal, @Valid @RequestBody FilmeRequest request) {
        return demo.create(principal, "FILME", request);
    }

    @PutMapping("/filmes/{id}")
    public ObjectNode updateFilme(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id, @Valid @RequestBody FilmeRequest request) {
        return demo.update(principal, "FILME", id, request);
    }

    @DeleteMapping("/filmes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilme(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) { demo.delete(principal, "FILME", id); }

    @GetMapping("/users")
    public List<ObjectNode> users(@AuthenticationPrincipal AppUserDetails principal) { return demo.list(principal, "USER"); }

    @GetMapping("/users/{id}")
    public ObjectNode user(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) { return demo.get(principal, "USER", id); }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ObjectNode createUser(@AuthenticationPrincipal AppUserDetails principal, @Valid @RequestBody DemoUserRequest request) {
        return demo.create(principal, "USER", request);
    }

    @PutMapping("/users/{id}")
    public ObjectNode updateUser(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id, @Valid @RequestBody DemoUserRequest request) {
        return demo.update(principal, "USER", id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) { demo.delete(principal, "USER", id); }
}
