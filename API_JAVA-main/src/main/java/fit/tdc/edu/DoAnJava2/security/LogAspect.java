package fit.tdc.edu.DoAnJava2.security;

import fit.tdc.edu.DoAnJava2.model.AuditLog;
import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import fit.tdc.edu.DoAnJava2.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    @AfterReturning(pointcut = "@annotation(logAction)", returning = "result")
    public void logActivity(JoinPoint joinPoint, LogAction logAction, Object result) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }
            String username = authentication.getName();
            if ("anonymousUser".equals(username)) {
                return;
            }

            // Tìm userId tương ứng
            Long userId = null;
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                userId = user.getId();
            }


            // Trích xuất thông tin đối tượng thao tác động (nếu có)
            String rawDesc = logAction.description();
            String actionType = logAction.actionType();
            String description = rawDesc;
            Object[] args = joinPoint.getArgs();
            
            // Xử lý động cho trạng thái xuất/nhập kho
            if ("CHANGE_STOCK_IN_STATUS".equals(actionType)) {
                ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (sra != null) {
                    String statusParam = sra.getRequest().getParameter("status");
                    if (statusParam != null) {
                        if (statusParam.equalsIgnoreCase("COMPLETED")) {
                            actionType = "APPROVE_STOCK_IN";
                            description = "Phê duyệt phiếu nhập kho #PN-" + args[0];
                        } else if (statusParam.equalsIgnoreCase("CANCELLED")) {
                            actionType = "CANCEL_STOCK_IN";
                            description = "Hủy bỏ chứng từ nhập kho #PN-" + args[0];
                        }
                    }
                }
            } else if ("CHANGE_STOCK_OUT_STATUS".equals(actionType)) {
                ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (sra != null) {
                    String statusParam = sra.getRequest().getParameter("status");
                    if (statusParam != null) {
                        if (statusParam.equalsIgnoreCase("COMPLETED")) {
                            actionType = "APPROVE_STOCK_OUT";
                            description = "Phê duyệt phiếu xuất kho #PX-" + args[0];
                        } else if (statusParam.equalsIgnoreCase("CANCELLED")) {
                            actionType = "CANCEL_STOCK_OUT";
                            description = "Hủy bỏ chứng từ xuất kho #PX-" + args[0];
                        }
                    }
                }
            } else if ("CREATE_STOCK_IN".equals(actionType)) {
                if (result != null) {
                    try {
                        java.lang.reflect.Method getIdMethod = result.getClass().getMethod("getId");
                        Object idVal = getIdMethod.invoke(result);
                        if (idVal != null) {
                            description = "Lập phiếu nhập kho chờ duyệt #PN-" + idVal;
                        }
                    } catch (Exception ignored) {}
                }
            } else if ("CREATE_STOCK_OUT".equals(actionType)) {
                if (result != null) {
                    try {
                        java.lang.reflect.Method getIdMethod = result.getClass().getMethod("getId");
                        Object idVal = getIdMethod.invoke(result);
                        if (idVal != null) {
                            description = "Lập phiếu xuất kho chờ duyệt #PX-" + idVal;
                        }
                    } catch (Exception ignored) {}
                }
            } else {
                // Xử lý mô tả động: ví dụ chèn thêm tên đối tượng nếu có trong tham số phương thức
                if (args != null && args.length > 0) {
                    Object firstArg = args[0];
                    if (firstArg != null) {
                        try {
                            // Thử lấy thuộc tính 'getName()' qua Reflection
                            java.lang.reflect.Method getNameMethod = firstArg.getClass().getMethod("getName");
                            String nameVal = (String) getNameMethod.invoke(firstArg);
                            if (nameVal != null && !nameVal.isEmpty()) {
                                description = description + " '" + nameVal + "'";
                            }
                        } catch (Exception ignored) {
                            if (firstArg instanceof Long || firstArg instanceof Integer) {
                                description = description + " " + firstArg;
                            }
                        }
                    }
                }
            }

            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setActionType(actionType);
            log.setDescription(description);
            log.setCreatedAt(LocalDateTime.now());

            auditLogService.saveLog(log);
        } catch (Exception e) {
            System.err.println("Lỗi ghi log hoạt động tự động: " + e.getMessage());
        }
    }
}
