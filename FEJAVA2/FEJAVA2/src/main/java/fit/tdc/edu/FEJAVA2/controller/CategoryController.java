package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public String viewCategories(
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
            Map<String, Object> response = categoryService.getCategoriesWithPagination(token, keyword, page, size, sort);
            model.addAttribute("list", response.get("content"));
            model.addAttribute("currentPage", response.get("number"));
            model.addAttribute("totalPages", response.get("totalPages"));
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            if ("XMLHttpRequest".equals(requestedWith)) return "categories :: #category-data-container";
            return "categories";

        } catch (Exception e) {
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            return "categories";
        }
    }

    @PostMapping("/categories/save")
    @ResponseBody
    public ResponseEntity<String> saveCategory(@RequestBody Map<String, Object> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");

        String role = (String) session.getAttribute("ROLE");
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Lỗi: Bạn không có quyền thực hiện thao tác này!");
        }

        try {
            return categoryService.saveCategoryToBackend(token, payload);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }

    @DeleteMapping("/categories/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");

        String role = (String) session.getAttribute("ROLE");
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Lỗi: Bạn không có quyền thực hiện thao tác này!");
        }

        try {
            return categoryService.deleteCategoryFromBackend(token, id);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server!");
        }
    }
}