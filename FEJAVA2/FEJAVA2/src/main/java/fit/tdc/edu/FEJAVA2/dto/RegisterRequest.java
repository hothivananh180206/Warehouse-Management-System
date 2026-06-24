package fit.tdc.edu.FEJAVA2.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String role;
    private String fullName;
    private String phone;
    private String email;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String confirmPassword, String role, String fullName, String phone, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Validation method
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

