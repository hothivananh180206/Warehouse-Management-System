package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.AuditLog;
import fit.tdc.edu.DoAnJava2.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/page")
    public Page<AuditLog> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "actionType", defaultValue = "") String actionType,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        // Sắp xếp lịch sử thao tác mới nhất lên đầu tiên
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        LocalDateTime startDateTime = null;
        if (startDate != null) {
            startDateTime = startDate.atStartOfDay(); // 00:00:00
        }

        LocalDateTime endDateTime = null;
        if (endDate != null) {
            endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999
        }

        return auditLogService.searchLogs(keyword.trim(), actionType.trim(), startDateTime, endDateTime, pageable);
    }
}
