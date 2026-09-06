package com.jobsearch.matchingservice.client.dto;

import java.util.List;


public record ScoreResult(
        Long jobId,
        Integer score,
        List<String> strengths,
        List<String> gaps,
        String explanation
) {
}
