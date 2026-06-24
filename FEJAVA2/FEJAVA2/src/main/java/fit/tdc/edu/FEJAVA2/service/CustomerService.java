package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.CustomerPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fit.tdc.edu.FEJAVA2.dto.Customer;

@Service
public class CustomerService {

    @Autowired
    private RestTemplate restTemplate;

    // Phải khớp với URL và cổng (Port 8080) chạy Backend của bạn
    private final String API_URL = (System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080") + "/customers";

    public CustomerPageResponse getCustomersWithPagination(String token, String keyword, int page, int size, String sort) {

        // In ra để bắt thủ phạm xem có nhận đúng chữ "Bùi" từ giao diện không
        System.out.println("FRONTEND nhận được từ khóa: " + keyword);

        // 1. Tạo Map chứa các tham số
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);

        // 2. Viết URL với biến giữ chỗ {}
        String url = API_URL + "/page?page={page}&size={size}&sort={sort}";

        // 3. Nếu có keyword, nhét thêm biến {keyword} vào
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 4. Truyền cục params vào, RestTemplate sẽ lo phần mã hóa Tiếng Việt (UTF-8)
        ResponseEntity<CustomerPageResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, CustomerPageResponse.class, params);
        System.out.println("FRONTEND nhận được từ khóa: " + keyword);
        return response.getBody();
    }
    public ResponseEntity<String> saveCustomerToBackend(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Lấy ID ra để kiểm tra xem là Thêm hay Sửa
        Object id = payload.get("id");

        if (id != null && !id.toString().isEmpty()) {
            // SỬA (PUT)
            return restTemplate.exchange(API_URL + "/" + id, HttpMethod.PUT, entity, String.class);
        } else {
            // THÊM (POST)
            return restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
        }
    }

    // HÀM 2: XÓA KHÁCH HÀNG
    public ResponseEntity<String> deleteCustomerFromBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(API_URL + "/" + id, HttpMethod.DELETE, entity, String.class);
    }
    // 🌟 THÊM HÀM NÀY ĐỂ KÉO TOÀN BỘ DANH SÁCH KHÁCH HÀNG (Phục vụ cho Modal Xuất Kho)
    public List<Customer> getAllCustomersList(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Customer[]> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, entity, Customer[].class);

        return Arrays.asList(response.getBody());
    }
}
