package com.example.onlineshopping.service;

import com.example.onlineshopping.entity.User;
import com.example.onlineshopping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRole()))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User.UserRole role) {
        // 直接使用枚举名称，因为已经是ROLE_前缀
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 确保角色不为空
        if (user.getRole() == null) {
            user.setRole(User.UserRole.ROLE_USER);
        }

        // 使用BCrypt加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        // 确保角色不为空
        if (user.getRole() == null) {
            user.setRole(User.UserRole.ROLE_USER);
        }

        // 如果是更新现有用户，检查密码是否需要重新加密
        if (user.getId() != null) {
            Optional<User> existingUser = userRepository.findById(user.getId());
            if (existingUser.isPresent()) {
                // 如果密码被修改（不等于原密码），则重新加密
                if (!user.getPassword().equals(existingUser.get().getPassword())) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }
            }
        } else {
            // 新用户，加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}