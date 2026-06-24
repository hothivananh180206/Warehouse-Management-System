package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<User> getUsersWithPage(String keyword, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by("username").ascending() : Sort.by("username").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchMultiFields(keyword, pageable);
    }

    // 🌟 LUỒNG 1: ADMIN TỰ TẠO TRONG TRANG QUẢN TRỊ -> ACTIVE LUÔN
    public User saveUserFromAdmin(User user) {
        user.setStatus("ACTIVE");
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // 🌟 LUỒNG 2: NHÂN VIÊN TỰ ĐĂNG KÝ TỪ NGOÀI -> MẶC ĐỊNH PENDING CHỜ DUYỆT
    public User registerUserFromOutside(User user) {
        user.setStatus("PENDING");
        user.setRole("STAFF"); // Mặc định tự đăng ký là Staff
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
    // 🌟 LUỒNG 3: VÔ HIỆU HÓA TÀI KHOẢN (KHÓA MÓM)


    // 🌟 LUỒNG 5: DUYỆT TÀI KHOẢN (TỪ PENDING -> ACTIVE)
    public void approveUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
        if (!"PENDING".equals(user.getStatus())) {
            throw new RuntimeException("Lỗi: Tài khoản không ở trạng thái chờ duyệt!");
        }
        user.setStatus("ACTIVE"); // Cấp thẻ xanh cho hoạt động
        userRepository.save(user);
    }

    // 🌟 LUỒNG XÓA
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Không thể thao tác trên tài khoản ADMIN cùng cấp!");
        }
        userRepository.deleteById(id);
    }

    // 🌟 LUỒNG KHÓA
    public void lockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Không thể thao tác trên tài khoản ADMIN cùng cấp!");
        }
        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    // 🌟 LUỒNG MỞ KHÓA (Cho chắc cốp)
    public void unlockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Không thể thao tác trên tài khoản ADMIN cùng cấp!");
        }
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }
    public List<User> getAllUsers() { return userRepository.findAll(); }
    public User getUserById(Long id) { return userRepository.findById(id).orElse(null); }


    public boolean isUsernameExists(String username) { return userRepository.existsByUsername(username); }
    public boolean isEmailExists(String email) { return userRepository.existsByEmail(email); }
    public boolean isPhoneExists(String phone) { return userRepository.existsByPhone(phone); }
    // 🌟 LẤY TOÀN BỘ DANH SÁCH CHỜ DUYỆT (KHÔNG PHÂN TRANG)
    public List<User> getPendingUsers() {
        return userRepository.findByStatus("PENDING");
    }

    // 🌟 THƯƠNG VỤ: CẬP NHẬT HỒ SƠ CÁ NHÂN (SELF PROFILE UPDATE)
    public User updateProfile(String username, User updatedInfo) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        // Kiểm tra trùng Email với người khác
        if (updatedInfo.getEmail() != null && !updatedInfo.getEmail().trim().isEmpty() 
            && !updatedInfo.getEmail().trim().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(updatedInfo.getEmail().trim())) {
                throw new RuntimeException("Lỗi: Email này đã được sử dụng bởi tài khoản khác!");
            }
            user.setEmail(updatedInfo.getEmail().trim());
        }

        // Kiểm tra trùng SĐT với người khác
        if (updatedInfo.getPhone() != null && !updatedInfo.getPhone().trim().isEmpty()
            && !updatedInfo.getPhone().trim().equalsIgnoreCase(user.getPhone())) {
            if (userRepository.existsByPhone(updatedInfo.getPhone().trim())) {
                throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng bởi tài khoản khác!");
            }
            user.setPhone(updatedInfo.getPhone().trim());
        }

        user.setFullName(updatedInfo.getFullName());
        return userRepository.save(user);
    }

    // ĐỔI MẬT KHẨU CÁ NHÂN (CHANGE PASSWORD WITH VERIFICATION)
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Lỗi: Mật khẩu hiện tại không chính xác!");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("Lỗi: Mật khẩu mới không được để trống!");
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);
    }

    //TỰ HỦY/VÔ HIỆU HÓA TÀI KHOẢN CÁ NHÂN (SELF DEACTIVATE)
    public void selfDeactivate(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        // Nếu là ADMIN, chặn không cho tự hủy nếu là ADMIN hoạt động duy nhất
        if ("ADMIN".equals(user.getRole())) {
            long activeAdminCount = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equals(u.getRole()) && "ACTIVE".equals(u.getStatus()))
                    .count();
            if (activeAdminCount <= 1) {
                throw new RuntimeException("Lỗi bảo mật: Bạn là tài khoản ADMIN duy nhất đang hoạt động, không thể tự vô hiệu hóa!");
            }
        }

        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));
    }
}