package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.Category;
import fit.tdc.edu.FEJAVA2.dto.ProductPageResponse;
import fit.tdc.edu.FEJAVA2.service.CategoryService;
import fit.tdc.edu.FEJAVA2.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/products")
    public String viewProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            Model model) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        try {
            // 1. Kéo data Sản phẩm (Nòng cốt - Bắt buộc sống)
            ProductPageResponse pageResponse = productService.getProductsWithPagination(token, keyword, page, size, sort);

            // 2. 🌟 TUYỆT CHIÊU CÁCH LY: Kéo data Danh mục (Lỡ Backend chưa có API thì cũng không bị sập trang Sản phẩm)
            List<Category> categories = new ArrayList<>();
            Map<Long, String> categoryMap = new HashMap<>();
            try {
                categories = categoryService.getAllCategories(token);
                if (categories != null) {
                    for (Category c : categories) {
                        categoryMap.put(c.getId(), c.getName());
                    }
                }
            } catch (Exception catError) {
                System.err.println("⚠️ Cảnh báo: Lấy danh mục thất bại (Backend chưa có API Category?): " + catError.getMessage());
            }

            // 3. Đổ data ra HTML
            model.addAttribute("categoryMap", categoryMap);
            model.addAttribute("categories", categories);
            model.addAttribute("list", pageResponse.getContent());
            model.addAttribute("currentPage", pageResponse.getNumber());
            model.addAttribute("totalPages", pageResponse.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            if ("XMLHttpRequest".equals(requestedWith)) {
                return "products :: #product-data-container";
            }
            return "products";

        } catch (Exception e) {
            System.err.println("❌ LỖI KẾT NỐI SẢN PHẨM: " + e.getMessage());

            // Gắn Data mồi chống sập màn hình đen
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryMap", new HashMap<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            return "products";
        }
    }

    @PostMapping("/products/save")
    @ResponseBody
    public ResponseEntity<String> saveProduct(@RequestBody Map<String, Object> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");
        try {
            return productService.saveProductToBackend(token, payload);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }

    @DeleteMapping("/products/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteProduct(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");
        try {
            return productService.deleteProductFromBackend(token, id);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }

    @PostMapping("/products/upload-image")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên đăng nhập!");
        try {
            return productService.uploadImageToBackend(token, file);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }
}