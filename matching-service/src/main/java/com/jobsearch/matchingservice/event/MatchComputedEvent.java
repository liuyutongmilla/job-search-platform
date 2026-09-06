package com.jobsearch.matchingservice.event;


public record MatchComputedEvent(
        Long userId,
        Integer resumeVersion,
        int matchCount,
        Integer topScore,
        String engine
) {
}
