package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.dto.StockOutRequest;
import fit.tdc.edu.DoAnJava2.model.StockOut;
import fit.tdc.edu.DoAnJava2.model.StockOutItem;
import fit.tdc.edu.DoAnJava2.security.LogAction;
import fit.tdc.edu.DoAnJava2.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock-out")
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    @GetMapping
    public List<StockOut> getAll() {
        return stockOutService.getAllStockOuts();
    }

    // API Mắt Thần xem chi tiết đóng gói cả thông tin phiếu lẫn mảng mặt hàng
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        try {
            StockOut stockOut = stockOutService.getById(id);
            List<StockOutItem> items = stockOutService.getStockOutItems(id);

            // 🌟 TÍNH TỔNG TIỀN PHIẾU XUẤT: Lấy danh sách sản phẩm thuộc phiếu xuất, nhân số lượng với đơn giá xuất rồi tính tổng cộng ở Backend
            double totalAmount = items.stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();

            Map<String, Object> response = new HashMap<>();
            response.put("info", stockOut);
            response.put("items", items);
            response.put("totalAmount", totalAmount); // 👈 Trả tổng tiền đã tính về cho client
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    @LogAction(actionType = "CREATE_STOCK_OUT", description = "Lập phiếu xuất kho chờ duyệt")
    public ResponseEntity<?> create(@RequestBody StockOutRequest request) {
        try {
            return ResponseEntity.ok(stockOutService.createStockOut(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API duyệt/hủy phiếu xuất kho
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "CHANGE_STOCK_OUT_STATUS", description = "Thay đổi trạng thái phiếu xuất")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!status.equalsIgnoreCase("COMPLETED") && !status.equalsIgnoreCase("CANCELLED")) {
            return ResponseEntity.badRequest().body("Lỗi: Trạng thái không hợp lệ!");
        }
        try {
            return ResponseEntity.ok(stockOutService.changeStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Phân trang tìm kiếm nâng cao
    @GetMapping("/page")
    public Page<StockOut> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return stockOutService.getStockOutsWithPage(keyword, page, size, sort);
    }

    // API phục vụ cho Khung trượt Offcanvas danh sách chờ duyệt (Chỉ ADMIN)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockOut>> getPendingStockOuts() {
        return ResponseEntity.ok(stockOutService.getPendingStockOuts());
    }
}