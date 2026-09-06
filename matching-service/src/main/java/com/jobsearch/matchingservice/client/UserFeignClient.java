package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ResumeView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * Retrieves resume data from user-service.
 * Upstream: matching-service. Downstream: target service resolved through Kubernetes discovery.
 */
@FeignClient(name = "user-service", fallback = UserFallback.class)
// 负责调用其他微服务
public interface UserFeignClient {

    @GetMapping("/api/{userId}/resume")
    ResumeView fetchLatestResume(@PathVariable("userId") Long userId);
}
