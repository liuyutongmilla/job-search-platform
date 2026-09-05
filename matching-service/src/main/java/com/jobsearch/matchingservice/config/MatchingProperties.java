package com.jobsearch.matchingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 绑定 config server 里的 {@code matching.*}。
 *
 * <p>{@code candidateLimit} 直接决定 AI 调用成本（送去打分的职位数量），
 * 是最该放在配置中心的参数 —— 成本失控时不需要发版就能收紧。
 */
@ConfigurationProperties(prefix = "matching")
public record MatchingProperties(
        Integer candidateLimit,
        Integer minScore,
        Integer topN,
        Cache cache
) {
    public record Cache(Integer ttlMinutes) {
    }

    public int candidateLimitOr(int fallback) {
        return candidateLimit == null || candidateLimit <= 0 ? fallback : candidateLimit;
    }

    public int minScoreOr(int fallback) {
        return minScore == null ? fallback : minScore;
    }

    public int topNOr(int fallback) {
        return topN == null || topN <= 0 ? fallback : topN;
    }
}
