package com.oa.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    private Path absoluteUploadPath;

    @PostConstruct
    public void init() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        absoluteUploadPath = projectRoot.resolve(uploadPath).toAbsolutePath().normalize();
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("上传文件为空");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            
            if (extension == null || !isImageExtension(extension.toLowerCase())) {
                return ResponseEntity.badRequest().body("仅支持图片格式上传");
            }

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = absoluteUploadPath.resolve("avatar").resolve(datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetPath = uploadDir.resolve(filename);
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("url", "/uploads/avatar/" + datePath + "/" + filename);
            result.put("filename", filename);
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }

    private boolean isImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("bmp") || extension.equals("webp");
    }
}
