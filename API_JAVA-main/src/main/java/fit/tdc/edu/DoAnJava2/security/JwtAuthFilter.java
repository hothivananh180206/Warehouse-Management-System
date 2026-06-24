package fit.tdc.edu.DoAnJava2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lấy phong bì "Authorization" từ Header
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String username;

        // 2. Kiểm tra xem có phong bì không, và có bắt đầu bằng chữ "Bearer " không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Không có thẻ thì cho đi tiếp (để bác bảo vệ phía sau chặn lại)
            return;
        }

        // 3. Cắt chữ "Bearer " đi để lấy đúng cái lõi thẻ JWT (bắt đầu từ ký tự thứ 7)
        jwtToken = authHeader.substring(7);
        username = jwtService.extractUsername(jwtToken); // Dịch tên user từ thẻ

        // 4. Nếu có tên user và user này chưa được hệ thống chứng thực
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 5. Kiểm tra xem thẻ còn hạn không, có đúng là của user này không
            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                // Hợp lệ -> Báo cho Spring Security biết là "Người này thẻ chuẩn, cho qua!"
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Cho phép request đi tiếp vào Controller
        filterChain.doFilter(request, response);
    }
}