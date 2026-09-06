package com.jobsearch.jobservice.event;

import java.time.LocalDateTime;
import java.util.Set;


public record JobPostedEvent(
        Long jobId,
        String title,
        String city,
        Set<String> skills,
        LocalDateTime postedAt
) {
}
