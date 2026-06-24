package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.SupplierPageResponse;
import fit.tdc.edu.FEJAVA2.service.SupplierService;
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
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/suppliers")
    public String viewSuppliers(
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
            SupplierPageResponse response = supplierService.getSuppliersWithPagination(token, keyword, page, size, sort);
            model.addAttribute("list", response.getContent());
            model.addAttribute("currentPage", response.getNumber());
            model.addAttribute("totalPages", response.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            if ("XMLHttpRequest".equals(requestedWith)) return "suppliers :: #supplier-data-container";
            return "suppliers";

        } catch (Exception e) {
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            return "suppliers";
        }
    }

    @PostMapping("/suppliers/save")
    @ResponseBody
    public ResponseEntity<String> saveSupplier(@RequestBody Map<String, Object> payload, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return supplierService.saveSupplierToBackend(token, payload);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }

    @DeleteMapping("/suppliers/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteSupplier(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return ResponseEntity.status(401).body("Lỗi: Hết phiên!");
        try {
            return supplierService.deleteSupplierFromBackend(token, id);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }
}