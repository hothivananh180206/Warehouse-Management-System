package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // 🌟 Kiểm tra trùng lặp nâng cao
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    // Tìm tất cả user theo trạng thái
    List<User> findByStatus(String status);
    // 🌟 Tìm kiếm đa trường: Quét từ Username, Họ tên, SĐT cho đến Email
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchMultiFields(@Param("keyword") String keyword, Pageable pageable);
}