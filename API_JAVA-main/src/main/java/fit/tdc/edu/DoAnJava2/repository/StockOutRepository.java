package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.StockOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StockOutRepository extends JpaRepository<StockOut, Long> {

    // Lấy danh sách chờ duyệt cho Offcanvas
    List<StockOut> findByStatus(String status);

    // Tìm kiếm phục vụ xuất Excel theo khoảng ngày
    List<StockOut> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<StockOut> findByCreatedAtBetweenAndStatus(java.time.LocalDateTime start, java.time.LocalDateTime end, String status);

    // Tìm đích danh mã phiếu khi gõ #PX-1
    @Query("SELECT s FROM StockOut s WHERE s.id = :id")
    Page<StockOut> findByExactId(@Param("id") Long id, Pageable pageable);

    // Tìm kiếm thông minh 4 trường: Mã phiếu, Trạng thái, Tên khách hàng, Tên người lập
    @Query("SELECT s FROM StockOut s " +
            "LEFT JOIN User u ON s.userId = u.id " +
            "LEFT JOIN Customer c ON s.customerId = c.id " +
            "WHERE CAST(s.id AS string) = :keyword OR " +
            "s.status = :searchStatus OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<StockOut> searchMultiFields(@Param("keyword") String keyword, @Param("searchStatus") String searchStatus, Pageable pageable);

    // --- CÁC PHƯƠNG THỨC LỌC DỮ LIỆU THEO TÀI KHOẢN NHÂN VIÊN ---
    Page<StockOut> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT s FROM StockOut s WHERE s.id = :id AND s.userId = :userId")
    Page<StockOut> findByExactIdAndUserId(@Param("id") Long id, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM StockOut s " +
            "LEFT JOIN User u ON s.userId = u.id " +
            "LEFT JOIN Customer c ON s.customerId = c.id " +
            "WHERE s.userId = :userId AND (CAST(s.id AS string) = :keyword OR " +
            "s.status = :searchStatus OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StockOut> searchMultiFieldsByUserId(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("searchStatus") String searchStatus, Pageable pageable);
}