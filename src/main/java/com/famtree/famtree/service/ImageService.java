package com.famtree.famtree.service;

import com.famtree.famtree.util.SSHFileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    
    private final SSHFileUploader sshFileUploader;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    public String uploadImage(MultipartFile file) throws IOException {
        validateImage(file);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID().toString() + "_" + originalFilename;
        
        // Create temporary file
        Path tempFile = Files.createTempFile("upload_", "_" + filename);
        file.transferTo(tempFile.toFile());
        
        try {
            // Upload via SSH
            return sshFileUploader.uploadFile(tempFile.toString(), filename);
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image. Received type: " + contentType);
        }
        
        // 10MB limit
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    public byte[] getImage(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        Path filePath = Paths.get(uploadDir, filename);
        if (!Files.exists(filePath)) {
            throw new IOException("Image not found: " + filename);
        }
        
        return Files.readAllBytes(filePath);
    }
} 