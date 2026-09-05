package com.jobsearch.matchingservice.scorer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.matchingservice.client.dto.JobView;
import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 降级路径的测试 —— <b>这是最该写测试的地方</b>。
 *
 * <p>原因：正常路径（调 AI）你每天开发时都会走到，坏了立刻发现。
 * 降级路径只在故障时才执行，而故障发生时你没有机会调试。
 * 一个从没被测试过的 fallback，等于没有 fallback。
 *
 * <p>banking 项目的 fallback 也没有测试 —— 这是它的另一处欠债。
 */
class RuleBasedScorerTest {

    private final RuleBasedScorer scorer = new RuleBasedScorer(new ObjectMapper());

    private static final String RESUME_JSON = """
            {
              "skills": ["Java", "Spring Boot", "Kafka"],
              "city": "上海",
              "totalYearsOfExperience": 4
            }
            """;

    private static JobView job(Long id, String city, Integer years, String... skills) {
        return new JobView(id, "职位" + id, "公司", city, 20000, 40000, years, "描述", Set.of(skills));
    }

    @Test
    void perfectMatchScoresHigh() {
        ScoreRequest req = new ScoreRequest(1L, 1, RESUME_JSON,
                List.of(job(10L, "上海", 3, "Java", "Kafka")));

        ScoreResult r = scorer.score(req).getFirst();

        // 技能 2/2 → 70，城市一致 → 15，经验 4 ≥ 3 → 15
        assertThat(r.score()).isEqualTo(100);
        assertThat(r.strengths()).isNotEmpty();
        assertThat(r.gaps()).isEmpty();
    }

    @Test
    void missingSkillsAndWrongCityLowersScore() {
        ScoreRequest req = new ScoreRequest(1L, 1, RESUME_JSON,
                List.of(job(11L, "北京", 3, "Go", "Rust")));

        ScoreResult r = scorer.score(req).getFirst();

        // 技能 0/2 → 0，城市不符 → 0，经验达标 → 15
        assertThat(r.score()).isEqualTo(15);
        assertThat(r.gaps()).anyMatch(g -> g.contains("缺少技能"));
        assertThat(r.gaps()).anyMatch(g -> g.contains("城市不符"));
    }

    @Test
    void insufficientExperienceIsReportedAsGap() {
        ScoreRequest req = new ScoreRequest(1L, 1, RESUME_JSON,
                List.of(job(12L, "上海", 10, "Java")));

        ScoreResult r = scorer.score(req).getFirst();

        assertThat(r.gaps()).anyMatch(g -> g.contains("经验不足"));
        assertThat(r.score()).isLessThan(100);
    }

    /**
     * 这个用例是本类最重要的一个：降级路径<b>自己不能再挂</b>。
     * 简历 JSON 是坏的、是 null、字段缺失，都必须返回结果而不是抛异常 ——
     * 否则故障时用户看到的是 500，而不是降级后的结果。
     */
    @Test
    void malformedResumeJsonStillProducesResults() {
        for (String bad : new String[]{null, "", "   ", "not json at all", "{\"skills\": \"not an array\"}"}) {
            ScoreRequest req = new ScoreRequest(1L, 1, bad, List.of(job(13L, "上海", 3, "Java")));

            List<ScoreResult> results = scorer.score(req);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().score()).isBetween(0, 100);
        }
    }

    @Test
    void emptyJobListReturnsEmptyNotNull() {
        assertThat(scorer.score(new ScoreRequest(1L, 1, RESUME_JSON, List.of()))).isEmpty();
        assertThat(scorer.score(new ScoreRequest(1L, 1, RESUME_JSON, null))).isEmpty();
        assertThat(scorer.score(null)).isEmpty();
    }

    @Test
    void skillMatchingIsCaseInsensitive() {
        ScoreRequest req = new ScoreRequest(1L, 1, RESUME_JSON,
                List.of(job(14L, "上海", 3, "java", "KAFKA")));

        ScoreResult r = scorer.score(req).getFirst();

        assertThat(r.score()).isEqualTo(100);
    }
}
