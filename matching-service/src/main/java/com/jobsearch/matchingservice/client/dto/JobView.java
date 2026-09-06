package com.jobsearch.matchingservice.client.dto;

import java.util.Set;


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
