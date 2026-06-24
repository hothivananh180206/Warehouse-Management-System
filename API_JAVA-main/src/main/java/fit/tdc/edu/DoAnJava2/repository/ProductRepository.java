package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🌟 MA THUẬT TÌM KIẾM TOÀN NĂNG (OMNI-SEARCH) CHO SẢN PHẨM
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchMultiFields(@Param("keyword") String keyword, Pageable pageable);

    // Kiểm tra trùng tên
    boolean existsByName(String name);

    // --- CÁC HÀM CŨ GIỮ LẠI CHO LOGIC BÁO CÁO / XUẤT KHO ---
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByQuantityLessThan(int quantity);
    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.minQuantity")
    List<Product> findProductsBelowMinQuantity();

    List<Product> findByExportPriceBetween(double minPrice, double maxPrice);
    List<Product> findAllByOrderByNameAsc();
    List<Product> findAllByOrderByNameDesc();
    List<Product> findAllByOrderByExportPriceAsc();
    List<Product> findAllByOrderByExportPriceDesc();
}