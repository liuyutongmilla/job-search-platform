package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.JobView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class JobFallback implements JobFeignClient {

    private static final Logger log = LoggerFactory.getLogger(JobFallback.class);

    @Override
    public List<JobView> search(String city, Integer minSalary, Integer maxYears, String skill, Integer limit) {
        log.warn("job-service unavailable, returning empty candidate list");
        return List.of();
    }
}
