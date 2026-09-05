package com.jobsearch.matchingservice.client.dto;

import java.util.Set;

/** job-service 返回体的本地视图（只声明本服务用到的字段） */
public record JobView(
        Long jobId,
        String title,
        String company,
        String city,
        Integer minSalary,
        Integer maxSalary,
        Integer requiredYears,
        String description,
        Set<String> skills
) {
}
