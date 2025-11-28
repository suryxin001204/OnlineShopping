package com.example.onlineshopping.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "这是一个公开接口，无需认证");
        return response;
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> userEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "这是一个用户接口，需要USER角色");
        response.put("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return response;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "这是一个管理员接口，需要ADMIN角色");
        response.put("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return response;
    }

    @GetMapping("/auth")
    public Map<String, Object> authEndpoint() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个需要认证的接口");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", authentication.isAuthenticated());
        return response;
    }
}