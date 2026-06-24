package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.Supplier;
import fit.tdc.edu.FEJAVA2.dto.SupplierPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_URL = (System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080") + "/suppliers";

    public SupplierPageResponse getSuppliersWithPagination(String token, String keyword, int page, int size, String sort) {
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

        return restTemplate.exchange(url, HttpMethod.GET, entity, SupplierPageResponse.class, params).getBody();
    }

    public ResponseEntity<String> saveSupplierToBackend(String token, Map<String, Object> payload) {
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

    public ResponseEntity<String> deleteSupplierFromBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/" + id, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
    }
    // 🌟 THÊM HÀM NÀY ĐỂ KÉO DANH SÁCH CHO DROPDOWN
    public List<Supplier> getAllSuppliersList(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Supplier[]> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, entity, Supplier[].class);
        return Arrays.asList(response.getBody());
    }
}
