package com.jobsearch.matchingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.matchingservice.client.AiFeignClient;
import com.jobsearch.matchingservice.client.JobFeignClient;
import com.jobsearch.matchingservice.client.UserFeignClient;
import com.jobsearch.matchingservice.client.dto.JobView;
import com.jobsearch.matchingservice.client.dto.ResumeView;
import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResponse;
import com.jobsearch.matchingservice.client.dto.ScoreResult;
import com.jobsearch.matchingservice.config.MatchingProperties;
import com.jobsearch.matchingservice.dto.MatchDto;
import com.jobsearch.matchingservice.dto.MatchResponseDto;
import com.jobsearch.matchingservice.entity.JobMatch;
import com.jobsearch.matchingservice.entity.MatchEngine;
import com.jobsearch.matchingservice.event.MatchComputedEvent;
import com.jobsearch.matchingservice.exception.ResumeNotReadyException;
import com.jobsearch.matchingservice.exception.UpstreamUnavailableException;
import com.jobsearch.matchingservice.repository.JobMatchRepository;
import com.jobsearch.matchingservice.service.IMatchingService;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MatchingServiceImpl implements IMatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingServiceImpl.class);

    private static final int DEFAULT_CANDIDATE_LIMIT = 20;
    private static final int DEFAULT_MIN_SCORE = 60;
    private static final int DEFAULT_TOP_N = 5;

    private final UserFeignClient userClient;
    private final JobFeignClient jobClient;
    private final AiFeignClient aiClient;
    private final JobMatchRepository matchRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;
    private final MatchingProperties props;

    public MatchingServiceImpl(UserFeignClient userClient,
                               JobFeignClient jobClient,
                               AiFeignClient aiClient,
                               JobMatchRepository matchRepository,
                               StreamBridge streamBridge,
                               ObjectMapper objectMapper,
                               MatchingProperties props) {
        this.userClient = userClient;
        this.jobClient = jobClient;
        this.aiClient = aiClient;
        this.matchRepository = matchRepository;
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    
    @Override
    @Retry(name = "computeMatches")
    @Transactional
    @CacheEvict(cacheNames = "matchResults", key = "#userId")
    public MatchResponseDto computeMatches(Long userId) {

        
        ResumeView resume = userClient.fetchLatestResume(userId);
        if (resume == null) {
            throw new UpstreamUnavailableException("user-service 暂时不可用，无法获取简历");
        }
        if (!resume.isUsable()) {
            throw new ResumeNotReadyException(
                    "简历尚未解析完成（当前状态：%s），请稍后重试".formatted(resume.parseStatus()));
        }

        ResumeFacts facts = readFacts(resume.parsedJson());

        
        int candidateLimit = props.candidateLimitOr(DEFAULT_CANDIDATE_LIMIT);
        List<JobView> candidates = jobClient.search(
                facts.city(), facts.expectedSalary(), facts.years(), null, candidateLimit);

        if (candidates.isEmpty()) {
            log.info("no candidates for userId={} (job-service may be degraded)", userId);
            return new MatchResponseDto(userId, resume.version(), 0, List.of(),
                    true, "未取到候选职位：job-service 可能不可用，或确实没有符合硬条件的职位");
        }

        
        Map<Long, JobMatch> cached = new HashMap<>();
        List<JobView> needScoring = new ArrayList<>();
        for (JobView job : candidates) {
            matchRepository
                    .findByUserIdAndJobIdAndResumeVersion(userId, job.jobId(), resume.version())
                    .ifPresentOrElse(
                            m -> cached.put(job.jobId(), m),
                            () -> needScoring.add(job));
        }
        log.info("userId={} candidates={} cached={} toScore={}",
                userId, candidates.size(), cached.size(), needScoring.size());

        
        MatchEngine engine = MatchEngine.AI;
        boolean degraded = false;
        String degradedReason = null;

        if (!needScoring.isEmpty()) {
            ScoreResponse response = aiClient.score(new ScoreRequest(
                    userId, resume.version(), resume.parsedJson(), needScoring));

            if (response == null || response.results() == null) {
                throw new UpstreamUnavailableException("ai-service 返回空响应");
            }
            if ("RULE_BASED".equals(response.engine())) {
                engine = MatchEngine.RULE_BASED;
                degraded = true;
                degradedReason = "AI 分析服务暂时不可用，当前为快速匹配结果，解释质量较低";
            }

            
            Map<Long, JobView> jobById = new HashMap<>();
            needScoring.forEach(j -> jobById.put(j.jobId(), j));

            for (ScoreResult r : response.results()) {
                if (r == null || r.jobId() == null || !jobById.containsKey(r.jobId())) {
                    
                    
                    log.warn("discarding score for unknown jobId={} userId={}",
                            r == null ? null : r.jobId(), userId);
                    continue;
                }
                cached.put(r.jobId(), persist(userId, resume.version(), r, engine));
            }
        }

        
        Map<Long, JobView> allJobs = new HashMap<>();
        candidates.forEach(j -> allJobs.put(j.jobId(), j));

        int minScore = props.minScoreOr(DEFAULT_MIN_SCORE);
        List<MatchDto> matches = cached.values().stream()
                .filter(m -> m.getScore() >= minScore)
                .sorted(Comparator.comparingInt(JobMatch::getScore).reversed())
                .limit(props.topNOr(DEFAULT_TOP_N))
                .map(m -> toDto(m, allJobs.get(m.getJobId())))
                .toList();

        publishMatchComputed(userId, resume.version(), matches, engine);

        return new MatchResponseDto(userId, resume.version(), candidates.size(),
                matches, degraded, degradedReason);
    }

    
    @Override
    @Cacheable(cacheNames = "matchResults", key = "#userId")
    public MatchResponseDto fetchMatches(Long userId) {
        ResumeView resume = userClient.fetchLatestResume(userId);
        if (resume == null) {
            throw new UpstreamUnavailableException("user-service 暂时不可用");
        }
        List<JobMatch> stored =
                matchRepository.findByUserIdAndResumeVersionOrderByScoreDesc(userId, resume.version());

        boolean anyRuleBased = stored.stream().anyMatch(m -> m.getEngine() == MatchEngine.RULE_BASED);
        List<MatchDto> matches = stored.stream()
                .limit(props.topNOr(DEFAULT_TOP_N))
                
                
                .map(m -> toDto(m, null))
                .toList();

        return new MatchResponseDto(userId, resume.version(), stored.size(), matches,
                anyRuleBased, anyRuleBased ? "部分结果来自降级的规则打分，建议重新计算" : null);
    }

    private JobMatch persist(Long userId, Integer version, ScoreResult r, MatchEngine engine) {
        JobMatch match = matchRepository
                .findByUserIdAndJobIdAndResumeVersion(userId, r.jobId(), version)
                .orElseGet(JobMatch::new);

        match.setUserId(userId);
        match.setJobId(r.jobId());
        match.setResumeVersion(version);
        match.setScore(clampScore(r.score()));
        match.setStrengths(writeJson(r.strengths()));
        match.setGaps(writeJson(r.gaps()));
        match.setExplanation(r.explanation());
        match.setEngine(engine);
        match.setComputedAt(LocalDateTime.now());
        return matchRepository.save(match);
    }

    
    private static int clampScore(Integer score) {
        if (score == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, score));
    }

    private MatchDto toDto(JobMatch m, JobView job) {
        return new MatchDto(
                m.getJobId(),
                job == null ? null : job.title(),
                job == null ? null : job.company(),
                job == null ? null : job.city(),
                m.getScore(),
                readJsonList(m.getStrengths()),
                readJsonList(m.getGaps()),
                m.getExplanation(),
                m.getEngine().name(),
                m.getComputedAt());
    }

    private void publishMatchComputed(Long userId, Integer version, List<MatchDto> matches, MatchEngine engine) {
        Integer topScore = matches.isEmpty() ? null : matches.getFirst().score();
        MatchComputedEvent event =
                new MatchComputedEvent(userId, version, matches.size(), topScore, engine.name());
        boolean sent = streamBridge.send("matchComputed-out-0", event);
        log.info("published match-computed userId={} count={} success={}", userId, matches.size(), sent);
    }

    

    private ResumeFacts readFacts(String parsedJson) {
        try {
            JsonNode root = objectMapper.readTree(parsedJson);
            String city = root.path("city").isMissingNode() ? null : root.path("city").asText(null);
            Integer years = root.path("totalYearsOfExperience").isMissingNode()
                    ? null : root.path("totalYearsOfExperience").asInt();
            Integer expected = root.path("expectedSalary").isMissingNode()
                    ? null : root.path("expectedSalary").asInt();
            return new ResumeFacts(city, years, expected);
        } catch (Exception e) {
            log.warn("cannot read resume facts, falling back to unfiltered search: {}", e.getMessage());
            return new ResumeFacts(null, null, null);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readerForListOf(String.class).readValue(json);
        } catch (Exception e) {
            return List.of();
        }
    }

    private record ResumeFacts(String city, Integer years, Integer expectedSalary) {
    }
}
