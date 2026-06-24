package fit.tdc.edu.FEJAVA2.dto;

public class AuditLog {
    private Long id;
    private Long userId;
    private String username;
    private String actionType;
    private String description;
    private String createdAt;

    public AuditLog() {
        super();
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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
