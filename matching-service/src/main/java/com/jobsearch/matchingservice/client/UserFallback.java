package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ResumeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class UserFallback implements UserFeignClient {

    private static final Logger log = LoggerFactory.getLogger(UserFallback.class);

    @Override
    public ResumeView fetchLatestResume(Long userId) {
        log.warn("user-service unavailable, cannot fetch resume for userId={}", userId);
        return null;
    }
}
