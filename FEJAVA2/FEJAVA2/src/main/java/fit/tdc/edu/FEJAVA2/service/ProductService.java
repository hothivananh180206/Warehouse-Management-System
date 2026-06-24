package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.Product;
import fit.tdc.edu.FEJAVA2.dto.ProductPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_URL = (System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080") + "/products";

    public List<Product> getAllProducts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Product[]> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, entity, Product[].class);
        return Arrays.asList(response.getBody());
    }

    public ProductPageResponse getProductsWithPagination(String token, String keyword, int page, int size, String sort) {
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

        ResponseEntity<ProductPageResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, ProductPageResponse.class, params);
        return response.getBody();
    }

    // 🌟 HÀM 1: LƯU SẢN PHẨM (Đã được thêm vào để Controller gọi)
    public ResponseEntity<String> saveProductToBackend(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        Object idObj = payload.get("id");
        if (idObj == null || idObj.toString().trim().isEmpty()) {
            // THÊM MỚI
            return restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
        } else {
            // CẬP NHẬT
            Long id = Long.parseLong(idObj.toString());
            return restTemplate.exchange(API_URL + "/" + id, HttpMethod.PUT, entity, String.class);
        }
    }

    // 🌟 HÀM 2: XÓA SẢN PHẨM (Đã được thêm vào để Controller gọi)
    public ResponseEntity<String> deleteProductFromBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(API_URL + "/" + id, HttpMethod.DELETE, entity, String.class);
    }

    public ResponseEntity<String> uploadImageToBackend(String token, MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(API_URL + "/upload-image", HttpMethod.POST, entity, String.class);
    }
}
