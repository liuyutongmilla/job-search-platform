package com.jobsearch.userservice.service;

import com.jobsearch.userservice.dto.ResumeDto;
import com.jobsearch.userservice.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {

    UserDto createUser(UserDto userDto);

    UserDto fetchUser(Long userId);

    boolean updateUser(Long userId, UserDto userDto);

    /**
     * 上传简历。<b>立即返回</b>，解析在后台异步进行。
     *
     * @return 状态为 PENDING 的简历记录
     */
    ResumeDto uploadResume(Long userId, MultipartFile file);

    /** 查最新简历及解析状态 */
    ResumeDto fetchLatestResume(Long userId);

    /** 由 Kafka 消费者调用：回写解析结果 */
    void applyParseResult(Long resumeId, boolean success, String parsedJson, String errorMessage);
}
