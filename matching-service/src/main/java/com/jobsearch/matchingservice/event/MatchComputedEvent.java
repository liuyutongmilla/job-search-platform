package com.jobsearch.matchingservice.event;

/**
 * matching-service ──► topic {@code match-computed} ──► notification-service
 *
 * <p>只带"通知需要的最小信息"：用户是谁、有几条高分匹配、最高分多少。
 * notification-service 要详情自己回查 —— 这样匹配结果的结构变化不会破坏通知服务。
 */
public record MatchComputedEvent(
        Long userId,
        Integer resumeVersion,
        int matchCount,
        Integer topScore,
        String engine
) {
}
