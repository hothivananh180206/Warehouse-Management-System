package fit.tdc.edu.DoAnJava2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockOutRequest {
    private Long customerId;
    private Long userId;
    private List<StockOutItemRequest> items;

    // Getter & Setter
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<StockOutItemRequest> getItems() { return items; }
    public void setItems(List<StockOutItemRequest> items) { this.items = items; }
}