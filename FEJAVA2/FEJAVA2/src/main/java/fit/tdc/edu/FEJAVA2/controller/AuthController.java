package fit.tdc.edu.FEJAVA2.controller;
import fit.tdc.edu.FEJAVA2.dto.User;
import fit.tdc.edu.FEJAVA2.dto.LoginRequest;
import fit.tdc.edu.FEJAVA2.dto.RegisterRequest;
import fit.tdc.edu.FEJAVA2.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import fit.tdc.edu.FEJAVA2.service.UserService;
import java.util.Base64;
@Controller
public class AuthController {

    @Autowired
    private AuthService authService; // Dùng Service thay vì RestTemplate
    @Autowired
    private UserService userService;
    // ĐIỀU HƯỚNG ĐĂNG NHẬP
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginRequest loginRequest, Model model, HttpSession session) {
        try {
            String token = authService.login(loginRequest);
            if (token != null) {
                session.setAttribute("JWT_TOKEN", token);
                session.setAttribute("LOGGED_IN_USER", loginRequest.getUsername());
                User user = userService.findByUsername(loginRequest.getUsername(), token);
                if (user != null) {
                    session.setAttribute("USER_ID", user.getId()); // 🔥 ĐÂY LÀ DÒNG ĐẠI CA CẦN!
                }
                // 🌟 TUYỆT CHIÊU: CẮT ĐÔI TOKEN ĐỂ ĐỌC XEM NÓ LÀ ADMIN HAY STAFF
                String[] chunks = token.split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

                // Nếu trong ruột token có chứa chữ ROLE_ADMIN -> Cấp cờ ADMIN vào Session
                if (payload.contains("\"role\":\"ROLE_ADMIN\"")) {
                    session.setAttribute("ROLE", "ADMIN");
                } else {
                    session.setAttribute("ROLE", "STAFF");
                }

                return "redirect:/index";
            }
        } catch (HttpClientErrorException e) {
            String errorMsg = e.getResponseBodyAsString();

            // Kiểm tra thẳng xem Backend có gửi thông điệp Khóa/Chờ duyệt không
            if (errorMsg != null && (errorMsg.contains("chờ duyệt") || errorMsg.contains("vô hiệu hóa"))) {
                model.addAttribute("error", errorMsg); // In thẳng ra màn hình
            } else {
                model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác!");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi kết nối máy chủ Backend!");
        }
        return "login";
    }

    // ĐIỀU HƯỚNG ĐĂNG KÝ
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute RegisterRequest registerRequest, Model model) {
        try {
            // Gọi hàm register đã sửa đường dẫn bên Service
            authService.register(registerRequest);
            return "redirect:/login?success=true";
        } catch (HttpClientErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                model.addAttribute("error", errorMsg);
            } else {
                model.addAttribute("error", "Đăng ký thất bại: Dữ liệu không hợp lệ hoặc tài khoản đã tồn tại!");
            }
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể kết nối đến máy chủ!");
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}