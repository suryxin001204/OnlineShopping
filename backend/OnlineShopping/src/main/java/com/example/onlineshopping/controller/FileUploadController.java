package com.example.onlineshopping.controller;

import com.example.onlineshopping.entity.User;
import com.example.onlineshopping.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    private final UserService userService;
    private static final String UPLOAD_DIR = "uploads/avatars/";
    
    public FileUploadController(UserService userService) {
        this.userService = userService;
        // 确保上传目录存在
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        System.out.println("收到头像上传请求，用户: " + userDetails.getUsername());
        System.out.println("文件信息: 大小=" + file.getSize() + ", 类型=" + file.getContentType());
        
        if (file.isEmpty()) {
            System.out.println("文件为空");
            return ResponseEntity.badRequest().body("请选择文件");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            System.out.println("文件类型不正确: " + contentType);
            return ResponseEntity.badRequest().body("只能上传图片文件");
        }
        
        // 检查文件大小（限制2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            System.out.println("文件过大: " + file.getSize());
            return ResponseEntity.badRequest().body("文件大小不能超过2MB");
        }
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + filename;
            
            // 更新用户头像
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 删除旧头像文件（如果存在）
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    String oldFilename = user.getAvatarUrl().substring(user.getAvatarUrl().lastIndexOf("/") + 1);
                    Files.deleteIfExists(Paths.get(UPLOAD_DIR + oldFilename));
                } catch (Exception e) {
                    // 忽略删除失败
                }
            }
            
            user.setAvatarUrl(avatarUrl);
            userService.save(user);
            
            System.out.println("头像上传成功: " + avatarUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            response.put("message", "头像上传成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件上传失败：" + e.getMessage());
        }
    }
}
