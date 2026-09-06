package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResponse;
import com.jobsearch.matchingservice.scorer.RuleBasedScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


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
