package fit.tdc.edu.DoAnJava2.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "users") // Giữ nguyên để map chính xác vào DB của đại ca
public class User {
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "ID tự sinh")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Role mặc định là STAFF")
    private String role = "STAFF"; // ADMIN, STAFF

    // 🌟 4 TRƯỜNG MỚI ĐẠI CA YÊU CẦU
    private String fullName;

    @Column(unique = true)
    private String phone;

    @Column(unique = true)
    private String email;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Status mặc định là PENDING")
    private String status = "PENDING"; // PENDING, ACTIVE, INACTIVE

    public User() {
        super();
    }

    public User(Long id, String username, String password, String role, String fullName, String phone, String email, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    // --- GETTER VÀ SETTER ĐẦY ĐỦ ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}