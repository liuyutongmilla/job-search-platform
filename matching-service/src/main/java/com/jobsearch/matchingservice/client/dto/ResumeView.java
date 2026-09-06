package com.jobsearch.matchingservice.client.dto;


public record ResumeView(
        Long resumeId,
        Long userId,
        String parseStatus,
        Integer version,
        String parsedJson
) {
    
    public boolean isUsable() {
        return "DONE".equals(parseStatus) && parsedJson != null && !parsedJson.isBlank();
    }
}
