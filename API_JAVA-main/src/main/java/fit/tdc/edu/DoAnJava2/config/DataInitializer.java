package fit.tdc.edu.DoAnJava2.config;

import fit.tdc.edu.DoAnJava2.entity.User;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Tự động tạo một tài khoản Admin nếu database trống
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setFullName("Administrator");
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setEmail("admin@warehouse.com");
            userRepository.save(admin);
            System.out.println("Đã tự động tạo tài khoản Admin mặc định: admin / 123456");
        }
    }
}
