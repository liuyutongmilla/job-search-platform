package com.jobsearch.matchingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "Match", description = "单条匹配结果")
public record MatchDto(
        Long jobId,
        String jobTitle,
        String company,
        String city,
        Integer score,
        List<String> strengths,
        List<String> gaps,
        String explanation,

        @Schema(description = "AI = Claude 打分；RULE_BASED = 降级的规则打分")
        String engine,

        LocalDateTime computedAt
) {
}
