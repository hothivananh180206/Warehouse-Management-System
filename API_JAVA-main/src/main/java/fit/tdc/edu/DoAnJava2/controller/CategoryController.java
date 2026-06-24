package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.Category;
import fit.tdc.edu.DoAnJava2.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public Page<Category> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return categoryService.getCategoriesWithPagination(keyword, page, size, sort);
    }

    @GetMapping
    public List<Category> getAll() { return categoryService.getAllCategories(); }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) { return categoryService.getCategoryById(id); }

    // 🌟 CHẶN TRÙNG TÊN DANH MỤC
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Category category) {
        String name = category.getName();
        if (name != null && !name.trim().isEmpty() && categoryService.isNameExists(name.trim())) {
            return ResponseEntity.badRequest().body("Lỗi: Danh mục '" + name + "' đã tồn tại!");
        }
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    // 🌟 CHẶN TRÙNG TÊN THÔNG MINH KHI CẬP NHẬT
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category category) {
        Category existingCategory = categoryService.getCategoryById(id);
        if (existingCategory == null) return ResponseEntity.badRequest().body("Lỗi: Không tìm thấy danh mục!");

        String newName = category.getName();
        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equalsIgnoreCase(existingCategory.getName())) {
            if (categoryService.isNameExists(newName.trim())) {
                return ResponseEntity.badRequest().body("Lỗi: Tên danh mục '" + newName + "' bị trùng với danh mục khác!");
            }
        }

        existingCategory.setName(category.getName());
        return ResponseEntity.ok(categoryService.saveCategory(existingCategory));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Đã xóa danh mục thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: Danh mục này đang có sản phẩm!");
        }
    }
}