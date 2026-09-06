package com.jobsearch.matchingservice.scorer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.matchingservice.client.dto.JobView;
import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


@Component
public class RuleBasedScorer {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedScorer.class);

    private static final int SKILL_WEIGHT = 70;
    private static final int CITY_WEIGHT = 15;
    private static final int YEARS_WEIGHT = 15;

    private final ObjectMapper objectMapper;

    public RuleBasedScorer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ScoreResult> score(ScoreRequest request) {
        if (request == null || request.jobs() == null || request.jobs().isEmpty()) {
            return List.of();
        }

        ResumeFacts facts = extractFacts(request.parsedResumeJson());
        List<ScoreResult> results = new ArrayList<>(request.jobs().size());
        for (JobView job : request.jobs()) {
            results.add(scoreOne(facts, job));
        }
        return results;
    }

    private ScoreResult scoreOne(ResumeFacts facts, JobView job) {
        List<String> strengths = new ArrayList<>();
        List<String> gaps = new ArrayList<>();

        
        Set<String> required = lower(job.skills());
        Set<String> matched = new LinkedHashSet<>(required);
        matched.retainAll(facts.skills());

        int skillScore;
        if (required.isEmpty()) {
            
            skillScore = SKILL_WEIGHT / 2;
        } else {
            skillScore = (int) Math.round((double) matched.size() / required.size() * SKILL_WEIGHT);
            if (!matched.isEmpty()) {
                strengths.add("技能匹配：" + String.join("、", matched));
            }
            Set<String> missing = new LinkedHashSet<>(required);
            missing.removeAll(facts.skills());
            if (!missing.isEmpty()) {
                gaps.add("缺少技能：" + String.join("、", missing));
            }
        }

        
        int cityScore = 0;
        if (facts.city() == null || job.city() == null) {
            cityScore = CITY_WEIGHT / 2;
        } else if (facts.city().equalsIgnoreCase(job.city())) {
            cityScore = CITY_WEIGHT;
            strengths.add("城市一致：" + job.city());
        } else {
            gaps.add("城市不符：职位在 " + job.city() + "，候选人在 " + facts.city());
        }

        
        int yearsScore;
        Integer requiredYears = job.requiredYears();
        if (requiredYears == null || facts.years() == null) {
            yearsScore = YEARS_WEIGHT / 2;
        } else if (facts.years() >= requiredYears) {
            yearsScore = YEARS_WEIGHT;
            strengths.add("经验达标：%d 年 ≥ 要求 %d 年".formatted(facts.years(), requiredYears));
        } else {
            yearsScore = 0;
            gaps.add("经验不足：%d 年 < 要求 %d 年".formatted(facts.years(), requiredYears));
        }

        int total = Math.min(100, skillScore + cityScore + yearsScore);
        String explanation = "【规则打分 · AI 服务降级中】技能 %d + 城市 %d + 经验 %d = %d 分。"
                .formatted(skillScore, cityScore, yearsScore, total);

        return new ScoreResult(job.jobId(), total, strengths, gaps, explanation);
    }

    
    private ResumeFacts extractFacts(String parsedResumeJson) {
        if (parsedResumeJson == null || parsedResumeJson.isBlank()) {
            return ResumeFacts.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(parsedResumeJson);

            Set<String> skills = new LinkedHashSet<>();
            JsonNode skillsNode = root.path("skills");
            if (skillsNode.isArray()) {
                skillsNode.forEach(n -> {
                    String s = n.asText("").trim().toLowerCase(Locale.ROOT);
                    if (!s.isEmpty()) {
                        skills.add(s);
                    }
                });
            }

            String city = root.path("city").isMissingNode() ? null : root.path("city").asText(null);
            Integer years = root.path("totalYearsOfExperience").isMissingNode()
                    ? null
                    : root.path("totalYearsOfExperience").asInt();

            return new ResumeFacts(skills, city, years);
        } catch (Exception e) {
            log.warn("failed to parse resume json in fallback scorer, using empty facts: {}", e.getMessage());
            return ResumeFacts.empty();
        }
    }

    private static Set<String> lower(Set<String> raw) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null) {
            return out;
        }
        for (String s : raw) {
            if (s != null && !s.isBlank()) {
                out.add(s.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private record ResumeFacts(Set<String> skills, String city, Integer years) {
        static ResumeFacts empty() {
            return new ResumeFacts(Set.of(), null, null);
        }
    }
}
