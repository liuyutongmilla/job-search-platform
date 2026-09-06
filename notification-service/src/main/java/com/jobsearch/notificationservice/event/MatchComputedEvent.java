package com.jobsearch.notificationservice.event;


public record MatchComputedEvent(
        Long userId,
        Integer resumeVersion,
        int matchCount,
        Integer topScore,
        String engine
) {
}
