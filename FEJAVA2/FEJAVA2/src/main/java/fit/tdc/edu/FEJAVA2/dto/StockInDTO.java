package fit.tdc.edu.FEJAVA2.dto;
import java.time.LocalDateTime;

public class StockInDTO {
    private Long id;
    private Long supplierId;
    private Long userId;
    private String createdAt; // Frontend hứng dạng chuỗi là an toàn nhất
    private String status;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}