package fit.tdc.edu.DoAnJava2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AuditLog() {
        super();
    }

    public AuditLog(Long id, Long userId, String username, String actionType, String description, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.description = description;
        this.createdAt = createdAt;
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
