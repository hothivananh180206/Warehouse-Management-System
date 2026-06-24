package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.StockIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDateTime;

public interface StockInRepository extends JpaRepository<StockIn, Long> {

    // Hàm lấy danh sách chờ (Offcanvas)
    List<StockIn> findByStatus(String status);

    // Tìm kiếm phục vụ xuất Excel theo khoảng ngày
    List<StockIn> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<StockIn> findByCreatedAtBetweenAndStatus(LocalDateTime start, LocalDateTime end, String status);

    // Tìm đích danh 1 ID (Khi gõ #PN-1)
    @Query("SELECT s FROM StockIn s WHERE s.id = :id")
    Page<StockIn> findByExactId(@Param("id") Long id, Pageable pageable);

    // 🌟 ĐÃ SỬA: Tách riêng cột Status ra, chỉ tìm chính xác (dấu =)
    @Query("SELECT s FROM StockIn s " +
            "LEFT JOIN User u ON s.userId = u.id " +
            "LEFT JOIN Supplier sp ON s.supplierId = sp.id " +
            "WHERE CAST(s.id AS string) = :keyword OR " +
            "s.status = :searchStatus OR " +   // <--- So sánh chính xác với biến trạng thái
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<StockIn> searchMultiFields(@Param("keyword") String keyword, @Param("searchStatus") String searchStatus, Pageable pageable);

    // --- CÁC PHƯƠNG THỨC LỌC DỮ LIỆU THEO TÀI KHOẢN NHÂN VIÊN ---
    Page<StockIn> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT s FROM StockIn s WHERE s.id = :id AND s.userId = :userId")
    Page<StockIn> findByExactIdAndUserId(@Param("id") Long id, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM StockIn s " +
            "LEFT JOIN User u ON s.userId = u.id " +
            "LEFT JOIN Supplier sp ON s.supplierId = sp.id " +
            "WHERE s.userId = :userId AND (CAST(s.id AS string) = :keyword OR " +
            "s.status = :searchStatus OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StockIn> searchMultiFieldsByUserId(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("searchStatus") String searchStatus, Pageable pageable);
}