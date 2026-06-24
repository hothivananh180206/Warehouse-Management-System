package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 🌟 THÊM HÀM NÀY: Kiểm tra trùng số điện thoại
    boolean existsByPhone(String phone);

    // Ma thuật quét đa trường OMNI-SEARCH
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phone LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Customer> searchMultiFields(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByName(String name);
    List<Customer> findByNameContainingIgnoreCase(String name);
    List<Customer> findByPhoneContaining(String phone);
    List<Customer> findByAddressContainingIgnoreCase(String address);
    List<Customer> findAllByOrderByNameAsc();
    List<Customer> findAllByOrderByNameDesc();
}