package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * Requests AI scoring from ai-service.
 * Upstream: matching-service. Downstream: target service resolved through Kubernetes discovery.
 */
@FeignClient(name = "ai-service", fallback = AiFallback.class)
public interface AiFeignClient {

    @PostMapping("/api/ai/score")
    ScoreResponse score(@RequestBody ScoreRequest request);
}
