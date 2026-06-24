package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // 🌟 Kiểm tra trùng Số điện thoại
    boolean existsByPhone(String phone);

    // Tìm kiếm đa trường (Tên, SĐT, Địa chỉ, Email nếu có)
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.phone LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Supplier> searchMultiFields(@Param("keyword") String keyword, Pageable pageable);
}