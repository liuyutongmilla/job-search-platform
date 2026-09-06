package com.jobsearch.userservice.event;


public record ResumeUploadedEvent(
        Long resumeId,
        Long userId,
        String fileKey,
        String contentType,
        Integer version
) {
}
