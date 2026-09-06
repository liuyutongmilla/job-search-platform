package com.jobsearch.matchingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


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
