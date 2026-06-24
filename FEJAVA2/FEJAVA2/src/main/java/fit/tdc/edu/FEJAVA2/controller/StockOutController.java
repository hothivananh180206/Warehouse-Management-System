package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.Customer;
import fit.tdc.edu.FEJAVA2.dto.User;
import fit.tdc.edu.FEJAVA2.dto.StockOutPageResponse;
import fit.tdc.edu.FEJAVA2.service.CustomerService;
import fit.tdc.edu.FEJAVA2.service.ProductService;
import fit.tdc.edu.FEJAVA2.service.StockOutService;
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
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService; // Đại ca đã ghim file này từ trước

    @Autowired
    private UserService userService;

    @GetMapping("/stock-out")
    public String viewStockOut(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session, Model model) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        try {
            // 1. Cung cấp danh sách sản phẩm phục vụ Modal lập phiếu
            model.addAttribute("products", productService.getAllProducts(token));

            // 2. Gọi Service kéo trang dữ liệu DTO
            StockOutPageResponse response = stockOutService.getStockOutsWithPagination(token, keyword, page, size, sort);
            model.addAttribute("list", response.getContent());
            model.addAttribute("currentPage", response.getNumber());
            model.addAttribute("totalPages", response.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);

            // 3. Đọc danh sách Khách hàng chế tạo Bản đồ hiển thị Tên Khách Hàng
            List<Customer> customers = customerService.getAllCustomersList(token);
            Map<String, String> customerMap = new HashMap<>();
            if (customers != null) {
                for (Customer c : customers) {
                    customerMap.put(String.valueOf(c.getId()), c.getName());
                }
            }
            model.addAttribute("customerMap", customerMap);
            model.addAttribute("customers", customers);

            // 4. Đọc danh sách User tạo Bản đồ hiển thị Tên Người Lập & Tài Khoản (Username)
            List<User> users = userService.getAllUsersList(token);
            Map<String, String> userMap = new HashMap<>();
            Map<String, String> usernameMap = new HashMap<>();
            if (users != null) {
                for (User u : users) {
                    String displayName = (u.getFullName() != null && !u.getFullName().trim().isEmpty()) ? u.getFullName() : u.getUsername();
                    userMap.put(String.valueOf(u.getId()), displayName);
                    usernameMap.put(String.valueOf(u.getId()), u.getUsername());
                }
            }
            model.addAttribute("userMap", userMap);
            model.addAttribute("usernameMap", usernameMap);

            // Hỗ trợ cập nhật AJAX ngầm tăng tốc SPA
            if ("XMLHttpRequest".equals(requestedWith)) {
                return "stock-out :: #stock-out-data-container";
            }

        } catch (Exception e) {
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("products", new ArrayList<>());
            model.addAttribute("customers", new ArrayList<>());
            model.addAttribute("customerMap", new HashMap<>());
            model.addAttribute("userMap", new HashMap<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            System.err.println("🔥 Lỗi tải trang Xuất Kho: " + e.getMessage());
        }
        return "stock-out";
    }

    @PostMapping("/stock-out/save")
    @ResponseBody
    public ResponseEntity<String> saveStockOut(@RequestBody Map<String, Object> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Hết phiên đăng nhập!");

        Object realUserId = session.getAttribute("USER_ID");
        if (realUserId != null) {
            payload.put("userId", realUserId);
        } else {
            return ResponseEntity.status(400).body("Lỗi: Không tìm thấy ID người dùng!");
        }

        try {
            return stockOutService.createStockOut(token, payload);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PutMapping("/stock-out/status/{id}")
    @ResponseBody
    public ResponseEntity<String> changeStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Hết phiên!");
        try {
            return stockOutService.updateStockOutStatus(token, id, status);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @GetMapping("/stock-out/detail/{id}")
    @ResponseBody
    public ResponseEntity<String> getStockOutDetail(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Hết phiên!");
        try {
            return stockOutService.getStockOutById(token, id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi kết nối máy chủ dữ liệu chính!");
        }
    }

    @GetMapping("/stock-out/pending")
    @ResponseBody
    public ResponseEntity<?> getPendingStockOuts(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi hết phiên!");

        String role = (String) session.getAttribute("ROLE");
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Lỗi: Bạn không có quyền xem danh sách chờ duyệt!");
        }

        try {
            return ResponseEntity.ok(stockOutService.getPendingStockOuts(token));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi đồng bộ danh sách chờ!");
        }
    }
}