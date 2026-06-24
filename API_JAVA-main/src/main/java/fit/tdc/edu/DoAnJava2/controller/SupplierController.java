package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.Supplier;
import fit.tdc.edu.DoAnJava2.security.LogAction;
import fit.tdc.edu.DoAnJava2.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/page")
    public Page<Supplier> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return supplierService.getSuppliersWithPage(keyword, page, size, sort);
    }

    @GetMapping
    public List<Supplier> getAll() { return supplierService.getAllSuppliers(); }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable Long id) { return supplierService.getSupplierById(id); }

    // 🌟 CHẶN TRÙNG SĐT KHI THÊM
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "CREATE_SUPPLIER", description = "Thêm mới nhà cung cấp")
    public ResponseEntity<?> create(@RequestBody Supplier supplier) {
        String phone = supplier.getPhone();
        if (phone != null && !phone.trim().isEmpty() && supplierService.isPhoneExists(phone.trim())) {
            return ResponseEntity.badRequest().body("Lỗi: Số điện thoại '" + phone + "' đã được đăng ký cho nhà cung cấp khác!");
        }
        return ResponseEntity.ok(supplierService.saveSupplier(supplier));
    }

    // 🌟 CHẶN TRÙNG SĐT KHI CẬP NHẬT (Thông minh)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "UPDATE_SUPPLIER", description = "Cập nhật thông tin nhà cung cấp")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        Supplier existing = supplierService.getSupplierById(id);
        if (existing == null) return ResponseEntity.badRequest().body("Lỗi: Không tìm thấy nhà cung cấp!");

        String newPhone = supplier.getPhone();
        if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.trim().equals(existing.getPhone())) {
            if (supplierService.isPhoneExists(newPhone.trim())) {
                return ResponseEntity.badRequest().body("Lỗi: Số điện thoại '" + newPhone + "' đã thuộc về nhà cung cấp khác!");
            }
        }

        existing.setName(supplier.getName());
        existing.setPhone(supplier.getPhone());
        existing.setAddress(supplier.getAddress());
        return ResponseEntity.ok(supplierService.saveSupplier(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "DELETE_SUPPLIER", description = "Xóa nhà cung cấp ID:")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok("Đã xóa nhà cung cấp thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}