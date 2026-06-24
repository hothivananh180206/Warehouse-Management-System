package fit.tdc.edu.FEJAVA2.controller;

import fit.tdc.edu.FEJAVA2.dto.AuditLogPageResponse;
import fit.tdc.edu.FEJAVA2.service.AuditLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private fit.tdc.edu.FEJAVA2.service.UserService userService;

    @GetMapping("/audit-logs")
    public String viewAuditLogs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String actionType,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            Model model) {

        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        try {
            // Đọc danh sách User tạo Bản đồ hiển thị Tên Đầy Đủ (fullName) trong Audit Logs
            java.util.List<fit.tdc.edu.FEJAVA2.dto.User> users = userService.getAllUsersList(token);
            java.util.Map<String, String> userFullNameMap = new java.util.HashMap<>();
            if (users != null) {
                for (fit.tdc.edu.FEJAVA2.dto.User u : users) {
                    userFullNameMap.put(String.valueOf(u.getId()), u.getFullName());
                }
            }
            model.addAttribute("userFullNameMap", userFullNameMap);

            AuditLogPageResponse pageResponse = auditLogService.getAuditLogs(token, keyword, actionType, startDate, endDate, page, size);
            model.addAttribute("list", pageResponse.getContent());
            model.addAttribute("currentPage", pageResponse.getNumber());
            model.addAttribute("totalPages", pageResponse.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("actionType", actionType);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("username", session.getAttribute("LOGGED_IN_USER"));

            if ("XMLHttpRequest".equals(requestedWith)) {
                return "audit-log :: #audit-log-data-container";
            }
        } catch (Exception e) {
            System.err.println("🔥 Lỗi tải trang lịch sử hoạt động: " + e.getMessage());
            model.addAttribute("list", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("actionType", actionType);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        }
        return "audit-log";
    }
}
