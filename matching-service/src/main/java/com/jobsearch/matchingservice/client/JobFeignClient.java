package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.JobView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Retrieves candidate jobs from job-service.
 * Upstream: matching-service. Downstream: target service resolved through Kubernetes discovery.
 */
@FeignClient(name = "job-service", fallback = JobFallback.class)
public interface JobFeignClient {

    
    @GetMapping("/api/search")
    List<JobView> search(@RequestParam(value = "city", required = false) String city,
                         @RequestParam(value = "minSalary", required = false) Integer minSalary,
                         @RequestParam(value = "maxYears", required = false) Integer maxYears,
                         @RequestParam(value = "skill", required = false) String skill,
                         @RequestParam(value = "limit", required = false) Integer limit);
}
