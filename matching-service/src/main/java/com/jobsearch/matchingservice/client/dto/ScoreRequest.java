package com.jobsearch.matchingservice.client.dto;

import java.util.List;


public record ScoreRequest(
        Long userId,
        Integer resumeVersion,
        String parsedResumeJson,
        List<JobView> jobs
) {
}
