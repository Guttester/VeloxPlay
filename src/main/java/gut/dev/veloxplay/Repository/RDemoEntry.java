package gut.dev.veloxplay.Repository;

import gut.dev.veloxplay.Model.DemoEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RDemoEntry extends JpaRepository<DemoEntry, Long> {
    List<DemoEntry> findByWorkspaceIdAndKindOrderById(String workspaceId, String kind);
    Optional<DemoEntry> findByIdAndWorkspaceIdAndKind(Long id, String workspaceId, String kind);
    long countByWorkspaceIdAndKind(String workspaceId, String kind);
    void deleteByWorkspaceId(String workspaceId);
    void deleteByWorkspaceExpiresAtBefore(Instant now);
}
