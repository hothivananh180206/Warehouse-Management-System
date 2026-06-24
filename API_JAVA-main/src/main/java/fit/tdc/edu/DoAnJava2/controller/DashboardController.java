package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.*;
import fit.tdc.edu.DoAnJava2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private StockOutItemRepository stockOutItemRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Tổng số loại sản phẩm
        long totalProducts = productRepository.count();
        stats.put("totalProducts", totalProducts);

        // 2. Tổng tồn kho
        List<Product> products = productRepository.findAll();
        long totalStock = products.stream().mapToLong(Product::getQuantity).sum();
        stats.put("totalStock", totalStock);

        // Fetch completed transactions
        List<StockOut> completedStockOuts = stockOutRepository.findByStatus("COMPLETED");
        List<StockIn> completedStockIns = stockInRepository.findByStatus("COMPLETED");

        // 3. Doanh thu xuất kho (COMPLETED)
        double totalExportRevenue = 0;
        for (StockOut so : completedStockOuts) {
            List<StockOutItem> items = stockOutItemRepository.findByStockOutId(so.getId());
            for (StockOutItem item : items) {
                totalExportRevenue += item.getQuantity() * item.getPrice();
            }
        }
        stats.put("totalExportRevenue", totalExportRevenue);

        // 4. Chi phí nhập kho (COMPLETED)
        double totalImportCost = 0;
        for (StockIn si : completedStockIns) {
            List<StockInItem> items = stockInItemRepository.findByStockInId(si.getId());
            for (StockInItem item : items) {
                totalImportCost += item.getQuantity() * item.getPrice();
            }
        }
        stats.put("totalImportCost", totalImportCost);

        // 5. Cơ cấu danh mục sản phẩm (Category distribution by stock quantity)
        List<Category> categories = categoryRepository.findAll();
        Map<Long, String> categoryNameMap = new HashMap<>();
        for (Category c : categories) {
            categoryNameMap.put(c.getId(), c.getName());
        }

        Map<String, Long> categoryCount = new HashMap<>();
        for (Product p : products) {
            String catName = categoryNameMap.getOrDefault(p.getCategoryId(), "Khác");
            categoryCount.put(catName, categoryCount.getOrDefault(catName, 0L) + p.getQuantity());
        }

        List<Map<String, Object>> categoryShares = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryCount.entrySet()) {
            Map<String, Object> share = new HashMap<>();
            share.put("categoryName", entry.getKey());
            share.put("quantity", entry.getValue());
            categoryShares.add(share);
        }
        stats.put("categoryShares", categoryShares);

        // 6. Top 5 sản phẩm bán chạy nhất
        List<Product> sortedProducts = new ArrayList<>(products);
        sortedProducts.sort((p1, p2) -> Integer.compare(p2.getTotalSold(), p1.getTotalSold()));
        List<Product> topSellers = sortedProducts.subList(0, Math.min(5, sortedProducts.size()));
        stats.put("topSellers", topSellers);

        // 7. Xu hướng Nhập - Xuất kho trong 6 tháng qua
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthTime = now.minusMonths(i);
            String monthLabel = "Tháng " + monthTime.getMonthValue();
            int year = monthTime.getYear();
            int month = monthTime.getMonthValue();

            double exportVal = 0;
            for (StockOut so : completedStockOuts) {
                if (so.getCreatedAt() != null && so.getCreatedAt().getYear() == year && so.getCreatedAt().getMonthValue() == month) {
                    List<StockOutItem> items = stockOutItemRepository.findByStockOutId(so.getId());
                    for (StockOutItem item : items) {
                        exportVal += item.getQuantity() * item.getPrice();
                    }
                }
            }

            double importVal = 0;
            for (StockIn si : completedStockIns) {
                if (si.getCreatedAt() != null && si.getCreatedAt().getYear() == year && si.getCreatedAt().getMonthValue() == month) {
                    List<StockInItem> items = stockInItemRepository.findByStockInId(si.getId());
                    for (StockInItem item : items) {
                        importVal += item.getQuantity() * item.getPrice();
                    }
                }
            }

            Map<String, Object> trendItem = new HashMap<>();
            trendItem.put("label", monthLabel);
            trendItem.put("exportValue", exportVal);
            trendItem.put("importValue", importVal);
            monthlyTrend.add(trendItem);
        }
        stats.put("monthlyTrend", monthlyTrend);

        return ResponseEntity.ok(stats);
    }
}
