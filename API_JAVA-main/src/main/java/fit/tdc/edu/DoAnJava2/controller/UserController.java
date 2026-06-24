package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.security.LogAction;
import fit.tdc.edu.DoAnJava2.service.UserService;
import fit.tdc.edu.DoAnJava2.dto.PasswordChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.security.Principal;
@RestController
@RequestMapping("/users")
public class    UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/page")
    public Page<User> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return userService.getUsersWithPage(keyword, page, size, sort);
    }


    // 🌟 API ADMIN TẠO MỚI TÀI KHOẢN (Set trạng thái ACTIVE luôn)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @LogAction(actionType = "CREATE_USER", description = "Đăng ký tài khoản nhân viên mới")
    public ResponseEntity<?> create(@RequestBody User user) {
        if (userService.isUsernameExists(user.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập này đã tồn tại!");
        }
        if (user.getEmail() != null && userService.isEmailExists(user.getEmail().trim())) {
            return ResponseEntity.badRequest().body("Lỗi: Email này đã tồn tại!");
        }
        if (user.getPhone() != null && userService.isPhoneExists(user.getPhone().trim())) {
            return ResponseEntity.badRequest().body("Lỗi: Số điện thoại này đã tồn tại!");
        }
        return ResponseEntity.ok(userService.saveUserFromAdmin(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Đã thu hồi tài khoản thành công!");
    }
    // 🌟 API KHÓA TÀI KHOẢN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long id) {
        try {
            userService.lockUser(id);
            return ResponseEntity.ok("Đã vô hiệu hóa tài khoản thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    // 🌟 API MỞ KHÓA TÀI KHOẢN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        try {
            userService.unlockUser(id);
            return ResponseEntity.ok("Đã mở khóa tài khoản thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    // 🌟 API DUYỆT TÀI KHOẢN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        try {
            userService.approveUser(id);
            return ResponseEntity.ok("Đã phê duyệt tài khoản thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    // 🌟 API TRẢ VỀ TOÀN BỘ DANH SÁCH PENDING
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }
    // 🌟 MỞ CỔNG CHO FRONTEND LẤY DANH SÁCH USER VỀ LÀM TỪ ĐIỂN
    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    // 🌟 API LẤY THÔNG TIN CÁ NHÂN (CHỈ XEM)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập!");
        }
        try {
            return ResponseEntity.ok(userService.getUserByUsername(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 🌟 API THAY ĐỔI MẬT KHẨU CÁ NHÂN
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    @LogAction(actionType = "CHANGE_PASSWORD", description = "Thay đổi mật khẩu cá nhân")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody PasswordChangeRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập!");
        }
        try {
            userService.changePassword(principal.getName(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Thay đổi mật khẩu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}