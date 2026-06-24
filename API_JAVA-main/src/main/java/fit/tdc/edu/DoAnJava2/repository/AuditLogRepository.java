package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:actionType IS NULL OR :actionType = '' OR a.actionType = :actionType) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate)")
    Page<AuditLog> searchLogs(@Param("keyword") String keyword,
                              @Param("actionType") String actionType,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);
}
