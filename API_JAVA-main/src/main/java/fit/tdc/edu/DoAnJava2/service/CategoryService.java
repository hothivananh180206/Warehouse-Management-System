package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.Category;
import fit.tdc.edu.DoAnJava2.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 🌟 HÀM PHÂN TRANG TOÀN NĂNG: VỪA MẶC ĐỊNH, VỪA TÌM KIẾM
    public Page<Category> getCategoriesWithPagination(String keyword, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by("name").ascending()
                : Sort.by("name").descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Nếu keyword rỗng -> Lấy tất cả
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll(pageable);
        }

        // Nếu có keyword -> Tìm theo tên
        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    // --- CÁC HÀM CRUD CƠ BẢN ---
    public List<Category> getAllCategories() { return categoryRepository.findAll(); }
    public Category getCategoryById(Long id) { return categoryRepository.findById(id).orElse(null); }
    public Category saveCategory(Category category) { return categoryRepository.save(category); }
    public void deleteCategory(Long id) { categoryRepository.deleteById(id); }
    public boolean isNameExists(String name) { return categoryRepository.existsByName(name); }
}