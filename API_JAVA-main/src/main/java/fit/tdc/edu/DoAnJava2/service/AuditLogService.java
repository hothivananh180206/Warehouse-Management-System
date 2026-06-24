package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.AuditLog;
import fit.tdc.edu.DoAnJava2.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Tìm kiếm và phân trang nhật ký hoạt động qua AOP Log
     */
    public Page<AuditLog> searchLogs(String keyword, String actionType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.searchLogs(keyword, actionType, startDate, endDate, pageable);
    }

    /**
     * Ghi nhận và lưu mới một log hoạt động hệ thống
     */
    public AuditLog saveLog(AuditLog log) {
        return auditLogRepository.save(log);
    }
}
