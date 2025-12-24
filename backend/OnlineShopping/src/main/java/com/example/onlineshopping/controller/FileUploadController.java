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
    private static final String AVATAR_UPLOAD_DIR = "uploads/avatars/";
    private static final String PRODUCT_UPLOAD_DIR = "uploads/products/";
    
    public FileUploadController(UserService userService) {
        this.userService = userService;
        // 确保上传目录存在
        try {
            Files.createDirectories(Paths.get(AVATAR_UPLOAD_DIR));
            Files.createDirectories(Paths.get(PRODUCT_UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("只能上传图片文件");
        }
        
        // 检查文件大小（限制2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
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
            Path filePath = Paths.get(AVATAR_UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 生成访问URL
            String avatarUrl = "/" + AVATAR_UPLOAD_DIR + filename;
            // 统一路径分隔符
            avatarUrl = avatarUrl.replace("\\", "/");
            
            // 更新用户头像
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 删除旧头像文件（如果存在）
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    String oldUrl = user.getAvatarUrl();
                    if (oldUrl.startsWith("/" + AVATAR_UPLOAD_DIR)) {
                        String oldFilename = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                        Files.deleteIfExists(Paths.get(AVATAR_UPLOAD_DIR + oldFilename));
                    }
                } catch (Exception e) {
                    // 忽略删除失败
                }
            }
            
            user.setAvatarUrl(avatarUrl);
            userService.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            response.put("message", "头像上传成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/product")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("只能上传图片文件");
        }
        
        // 检查文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("文件大小不能超过5MB");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(PRODUCT_UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 生成访问URL
            String fileUrl = "/" + PRODUCT_UPLOAD_DIR + filename;
            fileUrl = fileUrl.replace("\\", "/");
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件上传失败：" + e.getMessage());
        }
    }
}
