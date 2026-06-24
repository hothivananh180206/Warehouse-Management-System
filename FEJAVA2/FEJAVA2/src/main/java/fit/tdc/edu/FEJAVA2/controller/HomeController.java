package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.DashboardStats;
import fit.tdc.edu.FEJAVA2.service.DashboardService;
import fit.tdc.edu.FEJAVA2.service.ExportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

@Controller
public class HomeController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ExportService exportService;

    @GetMapping({"/", "/index", "/dashboard"})
    public String viewDashboard(HttpSession session, Model model) {
        // Kiểm tra xem người dùng đã có thẻ Token (đã đăng nhập) chưa
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đuổi về trang Login
        }

        // 🌟 NẠP DỮ LIỆU THỐNG KÊ KPI CHO THYMELEAF BẢO ĐẢM LOAD TỨC THÌ
        DashboardStats stats = dashboardService.getStats(token);
        model.addAttribute("stats", stats);

        // Truyền tên người dùng ra ngoài màn hình
        model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));
        return "index"; // Gọi file index.html
    }

    // 🌟 API PROXY CHO JAVASCRIPT AJAX ĐỂ VẼ BIỂU ĐỒ CHUYỂN ĐỘNG MƯỢT MÀ
    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public DashboardStats getDashboardStats(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            return null; // Chặn nếu chưa đăng nhập
        }
        return dashboardService.getStats(token);
    }

    @GetMapping("/api/export/stock-in")
    public ResponseEntity<byte[]> exportStockIn(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            ResponseEntity<byte[]> response = exportService.exportStockIn(token, startDate, endDate, status);
            return ResponseEntity.ok()
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/export/stock-out")
    public ResponseEntity<byte[]> exportStockOut(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            ResponseEntity<byte[]> response = exportService.exportStockOut(token, startDate, endDate, status);
            return ResponseEntity.ok()
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/export/products")
    public ResponseEntity<byte[]> exportProducts(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            ResponseEntity<byte[]> response = exportService.exportProducts(token);
            return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/export/categories")
    public ResponseEntity<byte[]> exportCategories(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            ResponseEntity<byte[]> response = exportService.exportCategories(token);
            return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/export/customers")
    public ResponseEntity<byte[]> exportCustomers(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            ResponseEntity<byte[]> response = exportService.exportCustomers(token);
            return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/export/suppliers")
    public ResponseEntity<byte[]> exportSuppliers(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            ResponseEntity<byte[]> response = exportService.exportSuppliers(token);
            return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/export/users")
    public ResponseEntity<byte[]> exportUsers(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            ResponseEntity<byte[]> response = exportService.exportUsers(token);
            return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}