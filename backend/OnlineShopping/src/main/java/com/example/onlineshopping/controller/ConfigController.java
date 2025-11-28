package com.example.onlineshopping.controller;

import com.example.onlineshopping.config.JwtConfig;
import com.example.onlineshopping.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final JwtConfig jwtConfig;
    private final JwtUtil jwtUtil;

    @GetMapping("/jwt")
    public Map<String, Object> getJwtConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("secretConfigured", jwtConfig.getSecret() != null);
        config.put("secretLength", jwtConfig.getSecret() != null ? jwtConfig.getSecret().length() : 0);
        config.put("expiration", jwtConfig.getExpiration());
        config.put("jwtUtilSecretLength", jwtUtil.getSecret() != null ? jwtUtil.getSecret().length() : 0);
        config.put("status", "OK");
        return config;
    }

    @GetMapping("/test-token")
    public Map<String, Object> testTokenGeneration() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 创建一个测试token
            String testToken = jwtUtil.generateToken(
                    org.springframework.security.core.userdetails.User
                            .withUsername("testuser")
                            .password("password")
                            .roles("USER")
                            .build()
            );
            result.put("status", "SUCCESS");
            result.put("tokenGenerated", true);
            result.put("tokenLength", testToken.length());
            result.put("username", jwtUtil.extractUsername(testToken));
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }
}