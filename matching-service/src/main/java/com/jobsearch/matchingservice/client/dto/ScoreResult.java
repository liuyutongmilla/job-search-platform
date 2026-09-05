package com.jobsearch.matchingservice.client.dto;

import java.util.List;

/**
 * ai-service 对单个职位的打分结果。
 *
 * <p>阶段 3 里，这个 record 会直接作为 Anthropic Java SDK 的
 * {@code .outputConfig(ScoreResult.class)} 参数 —— SDK 会从它自动推导 JSON Schema，
 * 保证模型返回的结构一定能反序列化成这个类型，不用手写 schema、不用手动 parse。
 */
public record ScoreResult(
        Long jobId,
        Integer score,
        List<String> strengths,
        List<String> gaps,
        String explanation
) {
}
