package com.jobsearch.userservice.storage;

import org.springframework.web.multipart.MultipartFile;


public interface ResumeStorage {

    
    String store(Long userId, MultipartFile file);

    
    byte[] read(String fileKey);
}
