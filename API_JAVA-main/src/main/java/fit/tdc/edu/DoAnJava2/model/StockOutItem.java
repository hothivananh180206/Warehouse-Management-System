package fit.tdc.edu.DoAnJava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_out_item")
public class StockOutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_out_id", nullable = false)
    private Long stockOutId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    // Constructors
    public StockOutItem() { super(); }

    // Getter và Setter
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