package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.Product;
import fit.tdc.edu.DoAnJava2.security.LogAction;
import fit.tdc.edu.DoAnJava2.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Lỗi: File tải lên trống!");
        }
        try {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path destPath = java.nio.file.Paths.get("uploads").toAbsolutePath().resolve(fileName);
            java.nio.file.Files.copy(file.getInputStream(), destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi tải ảnh lên: " + e.getMessage());
        }
    }

    @GetMapping("/page")
    public Page<Product> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return productService.getProductsWithPage(keyword, page, size, sort);
    }

    @GetMapping
    public List<Product> getAll() { return productService.getAllProducts(); }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) { return productService.getProductById(id); }

    // 🌟 1. XỬ LÝ THÊM MỚI: CHẶN TRÙNG TÊN SẢN PHẨM & ÉP GIÁ TRỊ BAN ĐẦU LÀ 0
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "CREATE_PRODUCT", description = "Thêm mới sản phẩm")
    public ResponseEntity<?> create(@RequestBody Product product) {
        String name = product.getName();
        if (name != null && !name.trim().isEmpty()) {
            if (productService.isNameExists(name.trim())) {
                return ResponseEntity.badRequest().body("Lỗi: Sản phẩm '" + name + "' đã tồn tại trong kho!");
            }
        }
        // Bảo mật Backend: Khởi tạo mặc định bằng 0, không cho phép gán tay ban đầu
        product.setImportPrice(0);
        product.setExportPrice(0);
        product.setQuantity(0);
        product.setTotalSold(0);
        
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    // 🌟 2. XỬ LÝ CẬP NHẬT: THÔNG MINH NÉ CHÍNH NÓ & KHÔNG CHO SỬA TAY SỐ LIỆU TỰ ĐỘNG
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "UPDATE_PRODUCT", description = "Cập nhật sản phẩm")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product product) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.badRequest().body("Lỗi: Không tìm thấy sản phẩm!");
        }

        String newName = product.getName();
        // Nếu đổi tên khác với tên cũ -> Check xem tên mới có bị trùng với SP khác không
        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equalsIgnoreCase(existingProduct.getName())) {
            if (productService.isNameExists(newName.trim())) {
                return ResponseEntity.badRequest().body("Lỗi: Tên sản phẩm '" + newName + "' đã bị trùng với một sản phẩm khác!");
            }
        }

        existingProduct.setName(product.getName());
        // Bảo mật Backend: Không gán lại importPrice, exportPrice và quantity từ client gửi lên để bảo toàn số liệu tự động tính toán
        existingProduct.setCategoryId(product.getCategoryId());
        existingProduct.setMinQuantity(product.getMinQuantity());

        // Kiểm tra xóa ảnh cũ nếu cập nhật ảnh mới
        String oldImagePath = existingProduct.getImagePath();
        String newImagePath = product.getImagePath();
        if (oldImagePath != null && !oldImagePath.isEmpty() && !oldImagePath.equals(newImagePath)) {
            try {
                java.nio.file.Path oldPath = java.nio.file.Paths.get("uploads").toAbsolutePath().resolve(oldImagePath);
                java.nio.file.Files.deleteIfExists(oldPath);
            } catch (Exception ignored) {}
        }
        existingProduct.setImagePath(newImagePath);

        return ResponseEntity.ok(productService.saveProduct(existingProduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "DELETE_PRODUCT", description = "Xóa sản phẩm ID:")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null && product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                try {
                    java.nio.file.Path filePath = java.nio.file.Paths.get("uploads").toAbsolutePath().resolve(product.getImagePath());
                    java.nio.file.Files.deleteIfExists(filePath);
                } catch (Exception ignored) {}
            }
            productService.deleteProduct(id);
            return ResponseEntity.ok("Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}