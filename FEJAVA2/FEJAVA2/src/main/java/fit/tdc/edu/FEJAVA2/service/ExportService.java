package fit.tdc.edu.FEJAVA2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class ExportService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BACKEND_EXPORT_URL = (System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080") + "/api/export";

    public ResponseEntity<byte[]> exportStockIn(String token, LocalDate startDate, LocalDate endDate, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        String url = BACKEND_EXPORT_URL + "/stock-in?startDate=" + startDate + "&endDate=" + endDate + "&status=" + status;

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    public ResponseEntity<byte[]> exportStockOut(String token, LocalDate startDate, LocalDate endDate, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        String url = BACKEND_EXPORT_URL + "/stock-out?startDate=" + startDate + "&endDate=" + endDate + "&status=" + status;

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    public ResponseEntity<byte[]> exportProducts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return restTemplate.exchange(BACKEND_EXPORT_URL + "/products", HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    public ResponseEntity<byte[]> exportCategories(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return restTemplate.exchange(BACKEND_EXPORT_URL + "/categories", HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    public ResponseEntity<byte[]> exportCustomers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return restTemplate.exchange(BACKEND_EXPORT_URL + "/customers", HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    public ResponseEntity<byte[]> exportSuppliers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return restTemplate.exchange(BACKEND_EXPORT_URL + "/suppliers", HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }

    public ResponseEntity<byte[]> exportUsers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return restTemplate.exchange(BACKEND_EXPORT_URL + "/users", HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
    }
}
