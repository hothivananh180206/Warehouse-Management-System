package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.StockInPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fit.tdc.edu.FEJAVA2.dto.StockInDTO;

@Service
public class StockInService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_IN_URL = "http://localhost:8080/stock-in";

    // 1. Lấy danh sách phiếu nhập
    public List<Map<String, Object>> getAllStockIns(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<List> response = restTemplate.exchange(
                API_IN_URL, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        return response.getBody();
    }

    // 2. Lấy chi tiết mặt hàng trong 1 phiếu nhập
    public List<Map<String, Object>> getStockInItems(String token, Long stockInId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<List> response = restTemplate.exchange(
                API_IN_URL + "/" + stockInId + "/items", HttpMethod.GET, new HttpEntity<>(headers), List.class);
        return response.getBody();
    }

    // 3. Tạo phiếu nhập mới
    public ResponseEntity<String> createStockIn(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(API_IN_URL, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
    }

    public ResponseEntity<String> updateStockInStatus(String token, Long id, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        // Thay PATCH thành PUT ở dòng dưới đây
        return restTemplate.exchange(API_IN_URL + "/" + id + "/status?status=" + status, HttpMethod.PUT, new HttpEntity<>(headers), String.class);

    }
    public ResponseEntity<String> getStockInById(String token, Long id) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        // Gọi sang Backend chính ở cổng 8080
        return restTemplate.exchange("http://localhost:8080/stock-in/" + id,
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class);
    }
    // 🌟 GỌI XUỐNG BACKEND LẤY PAGE
// 🌟 GỌI XUỐNG BACKEND LẤY PAGE
// 🌟 ĐÃ NÂNG CẤP DÙNG DTO CHUẨN MỰC
    public StockInPageResponse getStockInsWithPagination(String token, String keyword, int page, int size, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page); params.put("size", size); params.put("sort", sort);

        String url = "http://localhost:8080/stock-in/page?page={page}&size={size}&sort={sort}";
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        // Hứng thẳng vào StockInPageResponse
        return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, StockInPageResponse.class, params).getBody();
    }

    // 🌟 KÉO DANH SÁCH CHỜ DUYỆT (DÙNG MẢNG DTO)
    public List<StockInDTO> getPendingStockIns(String token) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        org.springframework.http.ResponseEntity<StockInDTO[]> response = restTemplate.exchange(
                "http://localhost:8080/stock-in/pending",
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                StockInDTO[].class);
        return Arrays.asList(response.getBody());
    }
}