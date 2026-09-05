package com.jobsearch.matchingservice.client.dto;

import java.util.List;

/**
 * ai-service 的响应外壳。
 *
 * <p>为什么要包一层，而不是直接返回 {@code List<ScoreResult>}：
 * 调用方需要知道<b>这批分数是谁算的</b>。Feign 的 fallback 触发时对调用方是
 * 完全透明的 —— 方法正常返回，没有异常，你无法从返回值判断刚才是不是降级了。
 *
 * <p>把 {@code engine} 放进契约，降级就变成了显式信息：能落库、能出监控指标、
 * 能告诉前端"当前是快速匹配模式"。
 *
 * <p>另一种常见写法是在 fallback 里设 ThreadLocal 标记，但那要小心清理，
 * 而且是隐式的。契约里多一个字段更直白。
 */
public record ScoreResponse(
        /** "AI" 或 "RULE_BASED" */
        String engine,
        List<ScoreResult> results
) {
    public static ScoreResponse ruleBased(List<ScoreResult> results) {
        return new ScoreResponse("RULE_BASED", results);
    }
}
