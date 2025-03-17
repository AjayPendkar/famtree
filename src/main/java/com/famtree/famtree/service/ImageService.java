package com.famtree.famtree.service;

import com.famtree.famtree.util.SSHFileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
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
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
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

    public byte[] getImage(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir, filename);
        return Files.readAllBytes(filePath);
    }
} 