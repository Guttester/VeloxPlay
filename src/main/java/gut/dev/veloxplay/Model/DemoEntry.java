package gut.dev.veloxplay.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "demo_entries", indexes = @Index(name = "demo_entries_workspace_kind_idx", columnList = "workspace_id,kind"))
public class DemoEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private DemoWorkspace workspace;
    @Column(nullable = false, length = 10)
    private String kind;
    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    public Long getId() { return id; }
    public DemoWorkspace getWorkspace() { return workspace; }
    public void setWorkspace(DemoWorkspace workspace) { this.workspace = workspace; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
