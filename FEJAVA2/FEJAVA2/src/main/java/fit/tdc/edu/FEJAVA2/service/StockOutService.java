package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.StockOutDTO;
import fit.tdc.edu.FEJAVA2.dto.StockOutPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockOutService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_OUT_URL = "http://localhost:8080/stock-out";

    // Kéo dữ liệu phân trang chuẩn DTO
    public StockOutPageResponse getStockOutsWithPagination(String token, String keyword, int page, int size, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page); params.put("size", size); params.put("sort", sort);

        String url = API_OUT_URL + "/page?page={page}&size={size}&sort={sort}";
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, StockOutPageResponse.class, params).getBody();
    }

    // Tạo phiếu xuất kho mới
    public ResponseEntity<String> createStockOut(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(API_OUT_URL, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
    }

    // Duyệt hoặc hủy phiếu xuất
    public ResponseEntity<String> updateStockOutStatus(String token, Long id, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_OUT_URL + "/" + id + "/status?status=" + status, HttpMethod.PUT, new HttpEntity<>(headers), String.class);
    }

    // Xem chi tiết phiếu (Mắt thần)
    public ResponseEntity<String> getStockOutById(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_OUT_URL + "/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    // Kéo danh sách chờ duyệt mảng DTO cho Offcanvas
    public List<StockOutDTO> getPendingStockOuts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<StockOutDTO[]> response = restTemplate.exchange(
                API_OUT_URL + "/pending", HttpMethod.GET, new HttpEntity<>(headers), StockOutDTO[].class);
        return Arrays.asList(response.getBody());
    }
}