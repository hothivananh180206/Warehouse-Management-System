package fit.tdc.edu.FEJAVA2.dto;

public class Product {
    private Long id;
    private String name;
    private double importPrice;
    private double exportPrice;
    private int quantity;
    private Long categoryId;
    private int minQuantity;
    private int totalSold;
    private String imagePath;

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