package com.jobsearch.matchingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


@Schema(name = "MatchResponse", description = "匹配结果集")
public record MatchResponseDto(
        Long userId,
        Integer resumeVersion,
        int totalCandidates,
        List<MatchDto> matches,

        @Schema(description = "true 表示本次结果由降级路径产生，质量低于正常水平")
        boolean degraded,

        @Schema(description = "降级原因，degraded=false 时为 null")
        String degradedReason
) {
}
