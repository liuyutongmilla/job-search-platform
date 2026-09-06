package com.jobsearch.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;


@Schema(name = "Resume", description = "简历及解析结果")
public record ResumeDto(
        Long resumeId,
        Long userId,
        String originalFilename,
        String parseStatus,
        Integer version,
        
        String parsedJson,
        String parseError,
        LocalDateTime uploadedAt
) {
}
