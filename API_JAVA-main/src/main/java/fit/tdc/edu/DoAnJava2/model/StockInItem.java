package fit.tdc.edu.DoAnJava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_in_item")
public class StockInItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_in_id", nullable = false)
    private Long stockInId; // Trỏ về ID của phiếu nhập (StockIn)

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    public StockInItem() {
        super();
    }

    public StockInItem(Long id, Long stockInId, Long productId, int quantity, double price) {
        this.id = id;
        this.stockInId = stockInId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStockInId() { return stockInId; }
    public void setStockInId(Long stockInId) { this.stockInId = stockInId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}