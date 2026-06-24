package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.CustomerPageResponse;
import fit.tdc.edu.FEJAVA2.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    @GetMapping("/customers")// Ví dụ: /customers
    public String viewCustomers(
            @RequestParam(defaultValue = "") String keyword, // Hứng keyword
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            Model model) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        try {
            CustomerPageResponse pageResponse = customerService.getCustomersWithPagination(token, keyword, page, size, sort);

            model.addAttribute("list", pageResponse.getContent());
            model.addAttribute("currentPage", pageResponse.getNumber());
            model.addAttribute("totalPages", pageResponse.getTotalPages());
            model.addAttribute("pageSize", size);

            // 🌟 BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ UI KHÔNG QUÊN TỪ KHÓA
            model.addAttribute("keyword", keyword);

            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            if ("XMLHttpRequest".equals(requestedWith)) {
                return "customers :: #customer-data-container"; // VD: customers :: #customer-data-container
            }

            return "customers";
        } catch (Exception e) {
            e.printStackTrace();
            return "customers";
        }
    }
    @PostMapping("/customers/save")
    @ResponseBody
    public ResponseEntity<String> saveCustomer(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");

        try {
            return customerService.saveCustomerToBackend(token, payload);
        }
        // 🌟 MA THUẬT LỌC LỖI Ở ĐÂY: Chỉ bóc lấy đúng câu thông báo của Backend
        catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }

    @DeleteMapping("/customers/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteCustomer(
            @PathVariable Long id,
            HttpSession session) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");

        try {
            return customerService.deleteCustomerFromBackend(token, id);
        }
        // 🌟 LỌC LỖI CHO CẢ HÀM XÓA
        catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }
}