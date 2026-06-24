package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String viewUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            Model model) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        // 🌟 BẢO VỆ VÒNG NGOÀI: NẾU KHÔNG PHẢI ADMIN THÌ SÚT VĂNG NGAY LẬP TỨC!
        String role = (String) session.getAttribute("ROLE");
        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/index"; // Đá về trang chủ không thương tiếc
        }

        try {
            Map<String, Object> response = userService.getUsersWithPagination(token, keyword, page, size, sort);
            model.addAttribute("list", response.get("content"));
            model.addAttribute("currentPage", response.get("number"));
            model.addAttribute("totalPages", response.get("totalPages"));
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            if ("XMLHttpRequest".equals(requestedWith)) return "users :: #user-data-container";
            return "users";

        } catch (Exception e) {
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            return "users";
        }
    }

    @PostMapping("/users/save")
    @ResponseBody
    public ResponseEntity<String> saveUser(@RequestBody Map<String, Object> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return userService.saveUserToBackend(token, payload);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }

    @DeleteMapping("/users/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return userService.deleteUserFromBackend(token, id);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }
    // 🌟 API NHẬN LỆNH TỪ NÚT BẤM TRÊN GIAO DIỆN
    @PutMapping("/users/lock/{id}")
    @ResponseBody
    public ResponseEntity<String> lockUser(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return userService.lockUserInBackend(token, id);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }
    // 🌟 API NHẬN LỆNH TỪ NÚT BẤM TRÊN GIAO DIỆN
    @PutMapping("/users/unlock/{id}")
    @ResponseBody
    public ResponseEntity<String> unlockUser(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return userService.unlockUserInBackend(token, id);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }
    // 🌟 API NHẬN LỆNH DUYỆT TỪ NÚT BẤM
    @PutMapping("/users/approve/{id}")
    @ResponseBody
    public ResponseEntity<String> approveUser(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return userService.approveUserInBackend(token, id);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }
    // 🌟 API TRẢ DATA CHO BẢNG PHỤ OFFCANVAS
    @GetMapping("/users/pending")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> getPendingUsers(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return org.springframework.http.ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");
        try {
            return org.springframework.http.ResponseEntity.ok(userService.getPendingUsers(token));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body("Lỗi khi tải danh sách chờ!");
        }
    }

    // 🌟 ROUTE MVC: HIỂN THỊ TRANG CÁ NHÂN (CHỈ XEM)
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        try {
            fit.tdc.edu.FEJAVA2.dto.User user = userService.getProfile(token);
            model.addAttribute("user", user);
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi tải thông tin cá nhân: " + e.getMessage());
            return "redirect:/index";
        }
    }

    // 🌟 API AJAX: THAY ĐỔI MẬT KHẨU CÁ NHÂN
    @PutMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            String oldPassword = payload.get("oldPassword");
            String newPassword = payload.get("newPassword");
            return userService.changePassword(token, oldPassword, newPassword);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}