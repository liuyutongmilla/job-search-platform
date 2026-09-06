package com.jobsearch.userservice.service;

import com.jobsearch.userservice.dto.ResumeDto;
import com.jobsearch.userservice.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {

    UserDto createUser(UserDto userDto);

    UserDto fetchUser(Long userId);

    boolean updateUser(Long userId, UserDto userDto);

    
    ResumeDto uploadResume(Long userId, MultipartFile file);

    
    ResumeDto fetchLatestResume(Long userId);

    
    void applyParseResult(Long resumeId, boolean success, String parsedJson, String errorMessage);
}
