package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.DashboardStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DashboardService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = (System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080") + "/api/dashboard";

    public DashboardStats getStats(String token) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (token != null) {
                headers.set("Authorization", "Bearer " + token);
            }
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<DashboardStats> response = restTemplate.exchange(
                    BASE_URL + "/stats", org.springframework.http.HttpMethod.GET, entity, DashboardStats.class);
            return response.getBody();
        } catch (Exception e) {
            // Fallback or log error
            System.err.println("Lỗi gọi API Dashboard Stats: " + e.getMessage());
            return new DashboardStats(); // Trả về đối tượng trống để tránh Crash giao diện
        }
    }
}
