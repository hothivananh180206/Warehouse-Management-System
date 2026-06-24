package fit.tdc.edu.FEJAVA2.service;

import fit.tdc.edu.FEJAVA2.dto.AuditLogPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditLogService {

    @Autowired
    private RestTemplate restTemplate;

    private final String API_URL = "http://localhost:8080/api/audit-logs";

    public AuditLogPageResponse getAuditLogs(String token, String keyword, String actionType, String startDate, String endDate, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);

        StringBuilder urlBuilder = new StringBuilder(API_URL).append("/page?page={page}&size={size}");

        if (keyword != null && !keyword.trim().isEmpty()) {
            urlBuilder.append("&keyword={keyword}");
            params.put("keyword", keyword.trim());
        }
        if (actionType != null && !actionType.trim().isEmpty()) {
            urlBuilder.append("&actionType={actionType}");
            params.put("actionType", actionType.trim());
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            urlBuilder.append("&startDate={startDate}");
            params.put("startDate", startDate.trim());
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            urlBuilder.append("&endDate={endDate}");
            params.put("endDate", endDate.trim());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<AuditLogPageResponse> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                entity,
                AuditLogPageResponse.class,
                params
        );
        return response.getBody();
    }
}
