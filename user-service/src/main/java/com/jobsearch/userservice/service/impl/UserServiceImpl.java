package com.jobsearch.userservice.service.impl;

import com.jobsearch.userservice.dto.ResumeDto;
import com.jobsearch.userservice.dto.UserDto;
import com.jobsearch.userservice.entity.AppUser;
import com.jobsearch.userservice.entity.ParseStatus;
import com.jobsearch.userservice.entity.Resume;
import com.jobsearch.userservice.event.ResumeUploadedEvent;
import com.jobsearch.userservice.exception.ResourceNotFoundException;
import com.jobsearch.userservice.exception.UserAlreadyExistsException;
import com.jobsearch.userservice.mapper.UserMapper;
import com.jobsearch.userservice.repository.ResumeRepository;
import com.jobsearch.userservice.repository.UserRepository;
import com.jobsearch.userservice.service.IUserService;
import com.jobsearch.userservice.storage.ResumeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeStorage resumeStorage;
    private final StreamBridge streamBridge;

    public UserServiceImpl(UserRepository userRepository,
                           ResumeRepository resumeRepository,
                           ResumeStorage resumeStorage,
                           StreamBridge streamBridge) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.resumeStorage = resumeStorage;
        this.streamBridge = streamBridge;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new UserAlreadyExistsException("邮箱已注册：" + userDto.email());
        }
        AppUser saved = userRepository.save(UserMapper.toNewEntity(userDto));
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto fetchUser(Long userId) {
        return UserMapper.toDto(requireUser(userId));
    }

    @Override
    @Transactional
    public boolean updateUser(Long userId, UserDto userDto) {
        AppUser user = requireUser(userId);
        UserMapper.applyEditableFields(userDto, user);
        userRepository.save(user);
        return true;
    }

    
    @Override
    @Transactional
    public ResumeDto uploadResume(Long userId, MultipartFile file) {
        requireUser(userId);

        String fileKey = resumeStorage.store(userId, file);
        int nextVersion = resumeRepository.findMaxVersion(userId).orElse(0) + 1;

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFileKey(fileKey);
        resume.setOriginalFilename(file.getOriginalFilename());
        resume.setContentType(file.getContentType());
        resume.setSizeBytes(file.getSize());
        resume.setParseStatus(ParseStatus.PENDING);
        resume.setVersion(nextVersion);
        resume.setUploadedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);

        
        
        
        
        
        
        ResumeUploadedEvent event = new ResumeUploadedEvent(
                saved.getResumeId(), userId, fileKey, saved.getContentType(), nextVersion);
        boolean sent = streamBridge.send("resumeUploaded-out-0", event);
        log.info("published resume-uploaded resumeId={} version={} success={}",
                saved.getResumeId(), nextVersion, sent);

        return UserMapper.toDto(saved);
    }

    @Override
    public ResumeDto fetchLatestResume(Long userId) {
        requireUser(userId);
        Resume resume = resumeRepository.findFirstByUserIdOrderByVersionDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "userId", String.valueOf(userId)));
        return UserMapper.toDto(resume);
    }

    
    @Override
    @Transactional
    public void applyParseResult(Long resumeId, boolean success, String parsedJson, String errorMessage) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "resumeId", String.valueOf(resumeId)));

        if (resume.getParseStatus() == ParseStatus.DONE && success) {
            log.debug("resumeId={} already DONE, ignoring duplicate event", resumeId);
            return;
        }

        if (success) {
            resume.setParseStatus(ParseStatus.DONE);
            resume.setParsedJson(parsedJson);
            resume.setParseError(null);
        } else {
            resume.setParseStatus(ParseStatus.FAILED);
            resume.setParseError(truncate(errorMessage, 1000));
        }
        resumeRepository.save(resume);
        log.info("applied parse result resumeId={} status={}", resumeId, resume.getParseStatus());
    }

    private AppUser requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
