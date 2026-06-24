package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.dto.StockInRequest;
import fit.tdc.edu.DoAnJava2.model.StockIn;
import fit.tdc.edu.DoAnJava2.model.StockInItem;
import fit.tdc.edu.DoAnJava2.security.LogAction;
import fit.tdc.edu.DoAnJava2.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/stock-in")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    // Xem tất cả phiếu nhập
    @GetMapping
    public List<StockIn> getAll() {
        return stockInService.getAllStockIns();
    }

    // Xem chi tiết các mặt hàng trong 1 phiếu nhập
//    @GetMapping("/{id}/items")
//    public List<StockInItem> getItems(@PathVariable Long id) {
//        return stockInService.getStockInItems(id);
//    }

    
    // 🌟 API XEM CHI TIẾT ĐÃ ĐƯỢC NÂNG CẤP (TRẢ VỀ CẢ PHIẾU LẪN MẶT HÀNG)
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        try {
            // 1. Lấy thông tin cơ bản của phiếu
            StockIn stockIn = stockInService.getById(id);

            // 2. Lấy danh sách các mặt hàng thuộc phiếu này
            List<StockInItem> items = stockInService.getStockInItems(id);

            // 🌟 TÍNH TỔNG TIỀN PHIẾU NHẬP: Lấy danh sách sản phẩm thuộc phiếu nhập, nhân số lượng với đơn giá rồi cộng dồn lại ở Backend
            double totalAmount = items.stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();

            // 3. Đóng gói cả 2 vào một cái Map (JSON object)
            Map<String, Object> response = new HashMap<>();
            response.put("info", stockIn);
            response.put("items", items); // 👈 Tên biến "items" này sẽ khớp 100% với Javascript ở Frontend
            response.put("totalAmount", totalAmount); // 👈 Đóng gói tổng tiền vào JSON để trả về cho Frontend hiển thị trực tiếp

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Tạo phiếu nhập kho MỚI
    @PostMapping
    @LogAction(actionType = "CREATE_STOCK_IN", description = "Lập phiếu nhập kho chờ duyệt")
    public ResponseEntity<?> create(@RequestBody StockInRequest request) {
        try {
            return ResponseEntity.ok(stockInService.createStockIn(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Thay @PatchMapping thành @PutMapping
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "CHANGE_STOCK_IN_STATUS", description = "Thay đổi trạng thái phiếu nhập")
    public Object updateStatus(@PathVariable Long id, @RequestParam String status) {
        // Chỉ cho phép 2 trạng thái này
        if (!status.equalsIgnoreCase("COMPLETED") && !status.equalsIgnoreCase("CANCELLED")) {
            return "Lỗi: Trạng thái không hợp lệ. Chỉ chấp nhận COMPLETED hoặc CANCELLED.";
        }

        try {
            return stockInService.changeStatus(id, status);
        } catch (RuntimeException e) {
            return "Lỗi: " + e.getMessage();
        }
    }
    // 🌟 API PHÂN TRANG TÌM KIẾM CHUẨN MỰC
    @GetMapping("/page")
    public org.springframework.data.domain.Page<StockIn> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return stockInService.getStockInsWithPage(keyword, page, size, sort);
    }
    // 🌟 API trả về danh sách chờ duyệt (Chỉ ADMIN)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockIn>> getPendingStockIns() {
        return ResponseEntity.ok(stockInService.getPendingStockIns());
    }
}