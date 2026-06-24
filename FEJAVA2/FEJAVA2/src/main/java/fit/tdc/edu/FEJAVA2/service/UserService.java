package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_URL = "http://localhost:8080/users";

    public Map<String, Object> getUsersWithPagination(String token, String keyword, int page, int size, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page); params.put("size", size); params.put("sort", sort);

        String url = API_URL + "/page?page={page}&size={size}&sort={sort}";
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class, params).getBody();
    }

    public ResponseEntity<String> saveUserToBackend(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(API_URL, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
    }

    public ResponseEntity<String> deleteUserFromBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/" + id, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
    }
    // 🌟 HÀM BẮN LỆNH KHÓA XUỐNG BACKEND
    public ResponseEntity<String> lockUserInBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/" + id + "/lock", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
    }
    // 🌟 HÀM BẮN LỆNH MỞ KHÓA XUỐNG BACKEND
    public ResponseEntity<String> unlockUserInBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/" + id + "/unlock", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
    }
    // 🌟 HÀM BẮN LỆNH DUYỆT XUỐNG BACKEND
    public ResponseEntity<String> approveUserInBackend(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/" + id + "/approve", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
    }
    // 🌟 HÀM KÉO DANH SÁCH CHỜ TỪ BACKEND
    public List<Map<String, Object>> getPendingUsers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/pending", HttpMethod.GET, new HttpEntity<>(headers), List.class).getBody();
    }
    // 🌟 THÊM HÀM NÀY ĐỂ KÉO DANH SÁCH USER LÀM MAP TÊN NGƯỜI LẬP
    public List<User> getAllUsersList(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<User[]> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, new HttpEntity<>(headers), User[].class);
        return Arrays.asList(response.getBody());
    }
    public User findByUsername(String username, String token) {
        // Gọi API sang Backend để lấy User theo username
        // Hoặc đại ca lấy danh sách rồi lọc ra trong list (cách này nhanh nhất nếu danh sách nhỏ)
        List<User> users = getAllUsersList(token);
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // 🌟 KÉO THÔNG TIN PROFILE CỦA TÀI KHOẢN ĐANG LOGGED IN
    public User getProfile(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return restTemplate.exchange(API_URL + "/profile", HttpMethod.GET, new HttpEntity<>(headers), User.class).getBody();
    }

    // 🌟 GỬI YÊU CẦU ĐỔI MẬT KHẨU
    public ResponseEntity<String> changePassword(String token, String oldPassword, String newPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> payload = new HashMap<>();
        payload.put("oldPassword", oldPassword);
        payload.put("newPassword", newPassword);
        return restTemplate.exchange(API_URL + "/change-password", HttpMethod.PUT, new HttpEntity<>(payload, headers), String.class);
    }
}