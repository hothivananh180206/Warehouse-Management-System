package fit.tdc.edu.DoAnJava2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockInRequest {
    private Long supplierId;
    private Long userId;
    private List<StockInItemRequest> items; // Danh sách các mặt hàng nhập

    // Getter & Setter
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<StockInItemRequest> getItems() { return items; }
    public void setItems(List<StockInItemRequest> items) { this.items = items; }
}