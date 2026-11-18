package gut.dev.veloxplay.Config;

import gut.dev.veloxplay.Repository.RDemoEntry;
import gut.dev.veloxplay.Repository.RDemoWorkspace;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.demo.cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class DemoCleanup {
    private final RDemoEntry entries;
    private final RDemoWorkspace workspaces;

    public DemoCleanup(RDemoEntry entries, RDemoWorkspace workspaces) {
        this.entries = entries;
        this.workspaces = workspaces;
    }

    @Scheduled(fixedDelayString = "${app.demo.cleanup-delay-ms:600000}")
    @Transactional
    public void cleanup() {
        Instant now = Instant.now();
        entries.deleteByWorkspaceExpiresAtBefore(now);
        entries.flush();
        workspaces.deleteByExpiresAtBefore(now);
    }
}
