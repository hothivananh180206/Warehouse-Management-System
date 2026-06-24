package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_URL = "http://localhost:8080/categories";

    // 🌟 HÀM ĐƯỢC BỔ SUNG: Dùng để ProductController gọi lấy dữ liệu đổ vào Dropdown
    public List<Category> getAllCategories(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Category[]> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, entity, Category[].class);

        if (response.getBody() != null) {
            return Arrays.asList(response.getBody());
        }
        return Collections.emptyList();
    }

    // --- CÁC HÀM XỬ LÝ CHO TRANG QUẢN LÝ DANH MỤC ---

    public Map<String, Object> getCategoriesWithPagination(String token, String keyword, int page, int size, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);

        String url = API_URL + "/page?page={page}&size={size}&sort={sort}";
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class, params).getBody();
    }

    public ResponseEntity<String> saveCategoryToBackend(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        Object idObj = payload.get("id");
        if (idObj == null || idObj.toString().trim().isEmpty()) {
            return restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
        } else {
            return restTemplate.exchange(API_URL + "/" + Long.parseLong(idObj.toString()), HttpMethod.PUT, entity, String.class);
        }
    }

    public ResponseEntity<String> deleteCategoryFromBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/" + id, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
    }
}