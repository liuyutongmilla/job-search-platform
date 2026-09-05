package com.jobsearch.matchingservice.client.dto;

import java.util.List;

/**
 * 发给 ai-service 的批量打分请求。
 *
 * <p>一次请求带多个职位，而不是每个职位调一次 —— 原因是 prompt 里的
 * 「评分标准 + 简历」这部分是共享的，批量调用能让这部分只传一次，
 * 配合 prompt caching 大幅省钱（阶段 3 会展开）。
 */
public record ScoreRequest(
        Long userId,
        Integer resumeVersion,
        String parsedResumeJson,
        List<JobView> jobs
) {
}
