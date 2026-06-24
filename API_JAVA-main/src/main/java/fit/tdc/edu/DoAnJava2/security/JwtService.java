package fit.tdc.edu.DoAnJava2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Khóa bí mật (Tuyệt đối không để lộ). Phải dài ít nhất 32 ký tự!
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    // 1. Hàm tạo ra thẻ JWT
    // 1. Hàm tạo ra thẻ JWT
    public String generateToken(UserDetails userDetails) {
        // 🌟 TẠO MỘT CÁI TÚI CHỨA QUYỀN (ROLE) ĐỂ NHÉT VÀO TOKEN
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        return Jwts.builder()
                .setClaims(claims) // Nhét túi quyền vào
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Hàm đọc tên người dùng từ cái thẻ JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Hàm kiểm tra xem thẻ JWT còn hạn và có đúng là của user đó không
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- CÁC HÀM PHỤ TRỢ BÊN DƯỚI DÙNG ĐỂ GIẢI MÃ ---
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        // Thay vì decode BASE64, hãy lấy Bytes trực tiếp từ chuỗi secretKey
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}