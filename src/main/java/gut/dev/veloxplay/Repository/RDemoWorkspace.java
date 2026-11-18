package gut.dev.veloxplay.Repository;

import gut.dev.veloxplay.Model.DemoWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;

public interface RDemoWorkspace extends JpaRepository<DemoWorkspace, String> {
    void deleteByExpiresAtBefore(Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select workspace from DemoWorkspace workspace where workspace.id = :id")
    Optional<DemoWorkspace> findLockedById(@Param("id") String id);
}
