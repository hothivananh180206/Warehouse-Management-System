package fit.tdc.edu.DoAnJava2.model;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 🌟 Chỉ đọc trên Swagger: Ẩn trường ID khi tạo mới sản phẩm (POST), chỉ hiển thị khi nhận kết quả (GET)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Mã tự sinh")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "import_price")
    // 🌟 Chỉ đọc trên Swagger: Ẩn giá nhập khi gửi dữ liệu lên, chỉ trả ra ở API hiển thị kết quả
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Giá nhập - Tự động tính toán")
    private double importPrice; // Giá nhập

    @Column(name = "export_price")
    // 🌟 Chỉ đọc trên Swagger: Ẩn giá bán ở Request Body, chỉ xuất hiện ở Response Body
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Giá bán - Tự động tính toán")
    private double exportPrice; // Giá xuất

    // 🌟 Chỉ đọc trên Swagger: Ẩn tồn kho ở biểu mẫu gửi lên để tránh sửa đổi số liệu tự động
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Tồn kho - Tự động tính toán")
    private int quantity; // Số lượng tồn kho hiện tại

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "min_quantity")
    private int minQuantity;

    @Column(name = "total_sold", nullable = false, columnDefinition = "int default 0")
    // 🌟 Chỉ đọc trên Swagger: Ẩn lũy kế bán ra của sản phẩm khi tạo mới/cập nhật sản phẩm
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Lũy kế đã bán - Tự động tính toán")
    private int totalSold; // Tổng số lượng đã bán tích lũy

    @Column(name = "image_path")
    private String imagePath;


    public Product() {
        super();
    }

    public Product(Long id, String name, double importPrice, double exportPrice, int quantity, Long categoryId, int minQuantity) {
        this.id = id;
        this.name = name;
        this.importPrice = importPrice;
        this.exportPrice = exportPrice;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.minQuantity = minQuantity;
        this.totalSold = 0;
    }

    public Product(Long id, String name, double importPrice, double exportPrice, int quantity, Long categoryId, int minQuantity, int totalSold) {
        this.id = id;
        this.name = name;
        this.importPrice = importPrice;
        this.exportPrice = exportPrice;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.minQuantity = minQuantity;
        this.totalSold = totalSold;
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getImportPrice() { return importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }

    public double getExportPrice() { return exportPrice; }
    public void setExportPrice(double exportPrice) { this.exportPrice = exportPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    public int getTotalSold() { return totalSold; }
    public void setTotalSold(int totalSold) { this.totalSold = totalSold; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}