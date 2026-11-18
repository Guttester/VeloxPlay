package gut.dev.veloxplay.Model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "demo_workspaces")
public class DemoWorkspace {
    @Id
    @Column(length = 36)
    private String id;
    @Column(nullable = false)
    private Instant expiresAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
