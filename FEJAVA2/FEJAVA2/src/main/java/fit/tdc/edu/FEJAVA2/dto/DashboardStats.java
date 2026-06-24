package fit.tdc.edu.FEJAVA2.dto;

import java.util.List;
import java.util.Map;

public class DashboardStats {
    private long totalProducts;
    private long totalStock;
    private double totalExportRevenue;
    private double totalImportCost;
    private List<Map<String, Object>> categoryShares;
    private List<Product> topSellers;
    private List<Map<String, Object>> monthlyTrend;

    // Default Constructor
    public DashboardStats() {}

    // Getters and Setters
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalStock() { return totalStock; }
    public void setTotalStock(long totalStock) { this.totalStock = totalStock; }

    public double getTotalExportRevenue() { return totalExportRevenue; }
    public void setTotalExportRevenue(double totalExportRevenue) { this.totalExportRevenue = totalExportRevenue; }

    public double getTotalImportCost() { return totalImportCost; }
    public void setTotalImportCost(double totalImportCost) { this.totalImportCost = totalImportCost; }

    public List<Map<String, Object>> getCategoryShares() { return categoryShares; }
    public void setCategoryShares(List<Map<String, Object>> categoryShares) { this.categoryShares = categoryShares; }

    public List<Product> getTopSellers() { return topSellers; }
    public void setTopSellers(List<Product> topSellers) { this.topSellers = topSellers; }

    public List<Map<String, Object>> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<Map<String, Object>> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
}
