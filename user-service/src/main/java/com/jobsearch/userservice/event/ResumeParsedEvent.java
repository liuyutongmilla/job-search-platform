package com.jobsearch.userservice.event;


public record ResumeParsedEvent(
        Long resumeId,
        Long userId,
        Integer version,
        boolean success,
        String parsedJson,
        String errorMessage
) {
}
