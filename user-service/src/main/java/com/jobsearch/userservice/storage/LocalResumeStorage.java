package com.jobsearch.userservice.storage;

import com.jobsearch.userservice.exception.ResumeStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Component
public class LocalResumeStorage implements ResumeStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalResumeStorage.class);

    private final Path root;

    public LocalResumeStorage(@Value("${user.resume.storage-dir:./data/resumes}") String storageDir) {
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResumeStorageException("上传文件为空");
        }
        try {
            Path userDir = root.resolve(String.valueOf(userId));
            Files.createDirectories(userDir);

            
            
            
            String key = userId + "/" + UUID.randomUUID() + extensionOf(file.getOriginalFilename());
            Path target = root.resolve(key).normalize();

            
            if (!target.startsWith(root)) {
                throw new ResumeStorageException("非法存储路径");
            }

            file.transferTo(target);
            log.debug("stored resume at {}", target);
            return key;
        } catch (IOException e) {
            throw new ResumeStorageException("保存简历文件失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] read(String fileKey) {
        Path target = root.resolve(fileKey).normalize();
        if (!target.startsWith(root)) {
            throw new ResumeStorageException("非法读取路径");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new ResumeStorageException("读取简历文件失败: " + e.getMessage());
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        String ext = filename.substring(dot).toLowerCase();
        
        return switch (ext) {
            case ".pdf", ".docx", ".doc", ".txt" -> ext;
            default -> "";
        };
    }
}
