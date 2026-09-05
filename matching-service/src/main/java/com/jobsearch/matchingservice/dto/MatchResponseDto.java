package com.jobsearch.matchingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 匹配接口的响应包装。
 *
 * <p>{@code degraded} 和 {@code degradedReason} 是<b>刻意暴露给客户端</b>的：
 * 降级必须是可见的。前端可以据此显示"AI 分析暂时不可用，当前为快速匹配结果"，
 * 用户就知道该不该信任这些解释。
 *
 * <p>只在服务端日志里记降级、对外假装一切正常，是在向用户隐瞒质量下降。
 */
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
