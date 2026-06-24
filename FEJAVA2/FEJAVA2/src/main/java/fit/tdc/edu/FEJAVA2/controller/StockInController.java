package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.Supplier;
import fit.tdc.edu.FEJAVA2.dto.User;
import fit.tdc.edu.FEJAVA2.service.ProductService;
import fit.tdc.edu.FEJAVA2.service.StockInService;
import fit.tdc.edu.FEJAVA2.service.SupplierService;
import fit.tdc.edu.FEJAVA2.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StockInController {

    @Autowired
    private StockInService stockService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private UserService userService;

    // 🌟 HÀM LẤY DANH SÁCH PHÂN TRANG THẬT (GỌN GÀNG, CHUẨN SÁCH GIÁO KHOA)
    @GetMapping("/stock-in")
    public String viewStockIn(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session, Model model) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        try {
            // 1. Lấy danh sách sản phẩm phục vụ cho ô chọn (Dropdown) Lập Phiếu
            model.addAttribute("products", productService.getAllProducts(token));

            // 🌟 2. CHỈ GỌI ĐÚNG 1 DÒNG NÀY ĐỂ KÉO PHÂN TRANG VÀ TÌM KIẾM TỪ BACKEND 8080
            // 🌟 Đổi Map thành DTO
            fit.tdc.edu.FEJAVA2.dto.StockInPageResponse response = stockService.getStockInsWithPagination(token, keyword, page, size, sort);

            // 3. Đẩy lên Model (Cú pháp Get thay vì get("key"))
            model.addAttribute("list", response.getContent());
            model.addAttribute("currentPage", response.getNumber());
            model.addAttribute("totalPages", response.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);

            // 4. Lấy danh sách NCC tạo Map hiển thị Tên (Giữ nguyên để HTML map được Tên)
            List<Supplier> suppliers = supplierService.getAllSuppliersList(token);
            Map<String, String> supplierMap = new HashMap<>();
            if (suppliers != null) {
                for (Supplier s : suppliers) {
                    supplierMap.put(String.valueOf(s.getId()), s.getName());
                }
            }
            model.addAttribute("supplierMap", supplierMap);
            model.addAttribute("suppliers", suppliers);

            // 5. Lấy danh sách User tạo Map hiển thị Tên & Tài Khoản (Username)
            List<User> users = userService.getAllUsersList(token);
            Map<String, String> userMap = new HashMap<>();
            Map<String, String> usernameMap = new HashMap<>();
            if (users != null) {
                for (User u : users) {
                    String displayName = (u.getFullName() != null && !u.getFullName().trim().isEmpty())
                            ? u.getFullName()
                            : u.getUsername();
                    userMap.put(String.valueOf(u.getId()), displayName);
                    usernameMap.put(String.valueOf(u.getId()), u.getUsername());
                }
            }
            model.addAttribute("userMap", userMap);
            model.addAttribute("usernameMap", usernameMap);

            // 🌟 TUYỆT CHIÊU SPA: Nếu là cuộc gọi ngầm AJAX từ Javascript, chỉ trả về đúng phân mảnh của cái Bảng
            if ("XMLHttpRequest".equals(requestedWith)) {
                return "stock-in :: #stock-in-data-container";
            }

        } catch (Exception e) {
            // Bọc giáp an toàn: Lỡ mất mạng hoặc lỗi DB thì set mặc định chống sập trang Thymeleaf
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("products", new ArrayList<>());
            model.addAttribute("suppliers", new ArrayList<>());
            model.addAttribute("supplierMap", new HashMap<>());
            model.addAttribute("userMap", new HashMap<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            System.err.println("🔥 Lỗi nghiêm trọng tại StockController: " + e.getMessage());
        }
        return "stock-in";
    }

    // 🌟 API TẠO PHIẾU NHẬP KHO MỚI
    @PostMapping("/stock-in/save")
    @ResponseBody
    public ResponseEntity<String> saveStockIn(@RequestBody Map<String, Object> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Hết phiên đăng nhập!");

        // LẤY ID THỰC TẾ CỦA NGƯỜI DÙNG ĐANG ĐĂNG NHẬP TỪ SESSION GÁN VÀO PHIẾU
        Object realUserId = session.getAttribute("USER_ID");
        if (realUserId != null) {
            payload.put("userId", realUserId);
        } else {
            return ResponseEntity.status(400).body("Lỗi: Không tìm thấy ID người dùng trong phiên đăng nhập!");
        }

        try {
            return stockService.createStockIn(token, payload);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    // 🌟 API DUYỆT / HỦY PHIẾU NHẬP
    @PutMapping("/stock-in/status/{id}")
    @ResponseBody
    public ResponseEntity<String> changeStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Hết phiên!");
        try {
            return stockService.updateStockInStatus(token, id, status);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    // 🌟 API KÉO THÔNG TIN CHI TIẾT (MẮT THẦN) GỌI SANG BACKEND 8080
    @GetMapping("/stock-in/detail/{id}")
    @ResponseBody
    public ResponseEntity<String> getStockInDetail(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Hết phiên đăng nhập!");

        try {
            return stockService.getStockInById(token, id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi kết nối Backend chính!");
        }
    }
    // 🌟 API MỞ CỔNG CHO AJAX GIAO DIỆN LẤY DỮ LIỆU (Chỉ ADMIN)
    @GetMapping("/stock-in/pending")
    @ResponseBody
    public ResponseEntity<?> getPendingStockIns(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");
        
        String role = (String) session.getAttribute("ROLE");
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Lỗi: Bạn không có quyền xem danh sách chờ duyệt!");
        }

        try {
            return ResponseEntity.ok(stockService.getPendingStockIns(token));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi tải danh sách chờ!");
        }
    }
}