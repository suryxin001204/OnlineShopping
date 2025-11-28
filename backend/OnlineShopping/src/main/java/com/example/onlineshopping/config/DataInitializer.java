package com.example.onlineshopping.config;

import com.example.onlineshopping.entity.User;
import com.example.onlineshopping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 初始化或更新管理员用户
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800138000");
            admin.setRole(User.UserRole.ROLE_ADMIN);
            System.out.println("创建管理员用户: admin/admin123");
        } else {
            System.out.println("更新管理员密码为加密格式");
        }
        // 强制更新密码为加密格式
        admin.setPassword(passwordEncoder.encode("admin123"));
        userRepository.save(admin);

        // 初始化或更新普通用户 user1
        User user1 = userRepository.findByUsername("user1").orElse(null);
        if (user1 == null) {
            user1 = new User();
            user1.setUsername("user1");
            user1.setEmail("user1@example.com");
            user1.setPhone("13800138001");
            user1.setRole(User.UserRole.ROLE_USER);
            System.out.println("创建普通用户: user1/user123");
        } else {
            System.out.println("更新user1密码为加密格式");
        }
        user1.setPassword(passwordEncoder.encode("user123"));
        userRepository.save(user1);
        
        // 初始化或更新普通用户 user
        User user = userRepository.findByUsername("user").orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPhone("13800138002");
            user.setRole(User.UserRole.ROLE_USER);
            System.out.println("创建普通用户: user/user123");
        } else {
            System.out.println("更新user密码为加密格式");
        }
        user.setPassword(passwordEncoder.encode("user123"));
        userRepository.save(user);

        System.out.println("数据初始化完成 - 所有密码已更新为BCrypt加密格式");
    }
}