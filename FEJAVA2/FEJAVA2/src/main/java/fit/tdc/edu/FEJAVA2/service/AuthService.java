package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.LoginRequest;
import fit.tdc.edu.FEJAVA2.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = (System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080") + "/api/auth";

    public String login(LoginRequest loginRequest) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                String.class
        );
        return response.getBody();
    }

    public String register(RegisterRequest registerRequest) {
        // Đã sửa thành /register theo Swagger của bạn
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registerRequest,
                String.class
        );
        return response.getBody();
    }
}
