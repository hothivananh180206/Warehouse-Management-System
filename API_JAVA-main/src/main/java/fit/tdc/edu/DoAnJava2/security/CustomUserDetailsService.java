package fit.tdc.edu.DoAnJava2.security;

import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user trong Database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));
        if ("PENDING".equals(user.getStatus())) {
            throw new org.springframework.security.authentication.LockedException("Tài khoản của bạn đang chờ duyệt!");
        }
        if ("INACTIVE".equals(user.getStatus())) {
            throw new org.springframework.security.authentication.LockedException("Tài khoản của bạn đã bị vô hiệu hóa!");
        }
        // Chuyển đổi User của mình thành UserDetails của Spring Security hiểu
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}