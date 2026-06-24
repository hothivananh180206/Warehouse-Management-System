package fit.tdc.edu.FEJAVA2.dto;

public class StockOutItemDTO {
    private Long id;
    private Long stockOutId;
    private Long productId;
    private int quantity;
    private double price;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStockOutId() { return stockOutId; }
    public void setStockOutId(Long stockOutId) { this.stockOutId = stockOutId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}