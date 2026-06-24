package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.AuthRequest;
import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import fit.tdc.edu.DoAnJava2.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // 1. API Đăng ký (Tạo tài khoản mới và Băm mật khẩu)
    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty() ||
            user.getFullName() == null || user.getFullName().trim().isEmpty() ||
            user.getEmail() == null || user.getEmail().trim().isEmpty() ||
            user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Lỗi: Vui lòng nhập đầy đủ thông tin bắt buộc!");
        }

        // Kiểm tra trùng tên đăng nhập
        if (userRepository.existsByUsername(user.getUsername().trim())) {
            return org.springframework.http.ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập này đã tồn tại!");
        }

        // Kiểm tra trùng Email
        if (userRepository.existsByEmail(user.getEmail().trim())) {
            return org.springframework.http.ResponseEntity.badRequest().body("Lỗi: Địa chỉ Email này đã tồn tại!");
        }

        // Kiểm tra trùng Số điện thoại
        if (userRepository.existsByPhone(user.getPhone().trim())) {
            return org.springframework.http.ResponseEntity.badRequest().body("Lỗi: Số điện thoại này đã tồn tại!");
        }

        // Tạo tài khoản an toàn
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim());
        user.setPhone(user.getPhone().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("STAFF");
        user.setStatus("ACTIVE"); // Sửa thành ACTIVE để demo không cần duyệt

        userRepository.save(user); // Lưu vào Database

        return org.springframework.http.ResponseEntity.ok("Đăng ký tài khoản thành công!");
    }



    // 2. API Đăng nhập (Kiểm tra pass và cấp thẻ JWT)
    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
            return org.springframework.http.ResponseEntity.ok(jwtService.generateToken(user));

        } catch (Exception e) {
            // 🌟 TUYỆT CHIÊU LỘT VỎ LỖI CỦA SPRING SECURITY
            String errorMsg = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errorMsg = e.getCause().getMessage(); // Moi cái ruột bên trong ra
            }

            // Nếu trong lỗi có chứa các từ khóa này, trả về đúng mã 403
            if (errorMsg != null && (errorMsg.contains("chờ duyệt") || errorMsg.contains("vô hiệu hóa"))) {
                return org.springframework.http.ResponseEntity.status(403).body(errorMsg);
            }

            // Còn lại thì chắc chắn là sai pass hoặc không tồn tại
            return org.springframework.http.ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không chính xác!");
        }
    }
}