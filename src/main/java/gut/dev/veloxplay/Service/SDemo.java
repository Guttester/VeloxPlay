package gut.dev.veloxplay.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gut.dev.veloxplay.Model.DemoEntry;
import gut.dev.veloxplay.Model.DemoWorkspace;
import gut.dev.veloxplay.Repository.*;
import gut.dev.veloxplay.dto.FilmeResponse;
import gut.dev.veloxplay.dto.AdminStatsResponse;
import gut.dev.veloxplay.exception.ApiException;
import gut.dev.veloxplay.exception.ResourceNotFoundException;
import gut.dev.veloxplay.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class SDemo {
    private final RDemoWorkspace workspaces;
    private final RDemoEntry entries;
    private final RFilme filmes;
    private final ObjectMapper mapper;
    private final long ttlMinutes;

    public SDemo(RDemoWorkspace workspaces, RDemoEntry entries, RFilme filmes, ObjectMapper mapper,
                 @Value("${app.demo.ttl-minutes:60}") long ttlMinutes) {
        this.workspaces = workspaces;
        this.entries = entries;
        this.filmes = filmes;
        this.mapper = mapper;
        if (ttlMinutes < 1 || ttlMinutes > 1440) throw new IllegalArgumentException("Demo TTL must be 1–1440 minutes");
        this.ttlMinutes = ttlMinutes;
    }

    public List<ObjectNode> list(AppUserDetails principal, String kind) {
        DemoWorkspace workspace = workspace(principal);
        return entries.findByWorkspaceIdAndKindOrderById(workspace.getId(), kind).stream().map(this::response).toList();
    }

    public ObjectNode get(AppUserDetails principal, String kind, Long id) {
        return response(find(principal, kind, id));
    }

    public ObjectNode create(AppUserDetails principal, String kind, Object request) {
        DemoWorkspace workspace = workspace(principal);
        int limit = kind.equals("FILME") ? 50 : 20;
        if (entries.countByWorkspaceIdAndKind(workspace.getId(), kind) >= limit) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "DEMO_LIMIT", "Limite de registros demonstrativos atingido");
        }
        return response(insert(workspace, kind, request));
    }

    public ObjectNode update(AppUserDetails principal, String kind, Long id, Object request) {
        DemoEntry entry = find(principal, kind, id);
        entry.setPayload(payload(request));
        return response(entries.save(entry));
    }

    public void delete(AppUserDetails principal, String kind, Long id) {
        entries.delete(find(principal, kind, id));
    }

    public void reset(AppUserDetails principal) {
        DemoWorkspace workspace = workspace(principal);
        entries.deleteByWorkspaceId(workspace.getId());
        entries.flush();
        seed(workspace);
    }

    public AdminStatsResponse stats(AppUserDetails principal) {
        String id = workspace(principal).getId();
        return new AdminStatsResponse(entries.countByWorkspaceIdAndKind(id, "FILME"),
                entries.countByWorkspaceIdAndKind(id, "USER"), 0);
    }

    private DemoWorkspace workspace(AppUserDetails principal) {
        Instant now = Instant.now();
        String id = principal.getSessionId();
        if (id == null || principal.getSessionExpiresAt() == null || !principal.getSessionExpiresAt().isAfter(now)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "DEMO_SESSION_REQUIRED", "Entre novamente para iniciar a demonstração");
        }
        DemoWorkspace existing = workspaces.findLockedById(id).orElse(null);
        if (existing != null) {
            if (!existing.getExpiresAt().isAfter(now)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "DEMO_SESSION_EXPIRED", "Demonstração expirada; entre novamente");
            }
            return existing;
        }
        if (workspaces.count() >= 200) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "DEMO_LIMIT", "Demonstração temporariamente ocupada");
        }
        DemoWorkspace created = new DemoWorkspace();
        created.setId(id);
        Instant ttl = now.plusSeconds(ttlMinutes * 60);
        created.setExpiresAt(ttl.isBefore(principal.getSessionExpiresAt()) ? ttl : principal.getSessionExpiresAt());
        workspaces.saveAndFlush(created);
        seed(created);
        return created;
    }

    private void seed(DemoWorkspace workspace) {
        // Only public movie data is copied. Real users, emails, passwords and favorites are never copied.
        filmes.findAll().stream().limit(40).forEach(filme -> insert(workspace, "FILME", FilmeResponse.from(filme)));
        insert(workspace, "USER", Map.of("nome", "Visitante exemplo", "email", "visitante@example.invalid", "role", "USER"));
        insert(workspace, "USER", Map.of("nome", "Moderador exemplo", "email", "moderador@example.invalid", "role", "MODERATOR"));
    }

    private DemoEntry insert(DemoWorkspace workspace, String kind, Object value) {
        DemoEntry entry = new DemoEntry();
        entry.setWorkspace(workspace);
        entry.setKind(kind);
        entry.setPayload(payload(value));
        return entries.save(entry);
    }

    private DemoEntry find(AppUserDetails principal, String kind, Long id) {
        return entries.findByIdAndWorkspaceIdAndKind(id, workspace(principal).getId(), kind)
                .orElseThrow(() -> new ResourceNotFoundException("Registro demonstrativo não encontrado"));
    }

    private String payload(Object value) {
        ObjectNode node = mapper.valueToTree(value);
        node.remove("id");
        return node.toString();
    }

    private ObjectNode response(DemoEntry entry) {
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(entry.getPayload());
            node.put("id", entry.getId());
            return node;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid demo record", ex);
        }
    }
}
