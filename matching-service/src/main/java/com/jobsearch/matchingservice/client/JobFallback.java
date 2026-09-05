package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.JobView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * job-service 不可用时返回空列表。
 *
 * <p>上层会把它当成"暂时没有匹配的职位"，返回一个空推荐列表给用户 ——
 * 比抛 500 好，但要在响应里标明这是降级结果（见 {@code MatchDto.degraded}），
 * 否则用户会以为真的没有合适职位。
 *
 * <p><b>降级必须是可观测的</b>：只降级不告警，等于把故障藏起来。
 */
@Component
public class JobFallback implements JobFeignClient {

    private static final Logger log = LoggerFactory.getLogger(JobFallback.class);

    @Override
    public List<JobView> search(String city, Integer minSalary, Integer maxYears, String skill, Integer limit) {
        log.warn("job-service unavailable, returning empty candidate list");
        return List.of();
    }
}
