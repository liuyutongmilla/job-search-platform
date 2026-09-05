package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResponse;
import com.jobsearch.matchingservice.scorer.RuleBasedScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <b>本项目里最重要的一个 fallback。</b>
 *
 * <p>AI 服务不可用（挂了、超时、或 API 额度用尽）时，不返回 null、不抛异常，
 * 而是<b>退化成规则打分</b>。用户仍然拿到推荐列表，只是解释质量下降。
 *
 * <p>这是从 banking 项目 {@code CardsFallback}（直接返回 null）往前走的一步：
 * 返回 null 是"丢掉这部分数据"，规则降级是"用便宜的方案顶上"。
 * 对求职推荐这种「有结果远比没结果有用」的场景，后者才是对的。
 *
 * <p>返回的 {@code engine = "RULE_BASED"} 会一路传到数据库和 API 响应，
 * 让降级变成可观测、可统计、可事后重算的事实。
 */
@Component
public class AiFallback implements AiFeignClient {

    private static final Logger log = LoggerFactory.getLogger(AiFallback.class);

    private final RuleBasedScorer ruleBasedScorer;

    public AiFallback(RuleBasedScorer ruleBasedScorer) {
        this.ruleBasedScorer = ruleBasedScorer;
    }

    @Override
    public ScoreResponse score(ScoreRequest request) {
        int jobCount = request == null || request.jobs() == null ? 0 : request.jobs().size();
        log.warn("ai-service unavailable, degrading to rule-based scoring for userId={} ({} jobs)",
                request == null ? null : request.userId(), jobCount);
        return ScoreResponse.ruleBased(ruleBasedScorer.score(request));
    }
}
