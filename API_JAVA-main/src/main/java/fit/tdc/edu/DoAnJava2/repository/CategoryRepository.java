package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 🌟 MA THUẬT TÌM KIẾM CÓ PHÂN TRANG CHO DANH MỤC
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Kiểm tra xem tên đã tồn tại chưa để báo lỗi trùng
    boolean existsByName(String name);

    // --- CÁC HÀM CŨ GIỮ LẠI (Cho Dropdown Thêm Sản Phẩm) ---
    List<Category> findByNameContainingIgnoreCase(String name);
    List<Category> findAllByOrderByNameAsc();
    List<Category> findAllByOrderByNameDesc();
}