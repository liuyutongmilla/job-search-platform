package com.jobsearch.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 简历状态与解析结果。
 *
 * <p>matching-service 通过 Feign 调 {@code GET /api/users/{id}/resume} 拿的就是这个。
 * 如果 {@code parseStatus != DONE}，matching-service 应该拒绝匹配并提示用户先等解析完成 ——
 * 这个判断放在调用方，因为"要不要降级"是调用方的业务决策。
 */
@Schema(name = "Resume", description = "简历及解析结果")
public record ResumeDto(
        Long resumeId,
        Long userId,
        String originalFilename,
        String parseStatus,
        Integer version,
        /** 结构化解析结果的 JSON 字符串；parseStatus != DONE 时为 null */
        String parsedJson,
        String parseError,
        LocalDateTime uploadedAt
) {
}
