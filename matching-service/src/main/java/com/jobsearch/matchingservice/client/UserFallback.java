package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ResumeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * user-service 不可用时的降级。
 *
 * <p>返回 {@code null} 而不是编一份假简历 —— 简历是匹配的必要输入，
 * 拿不到就必须让上层知道"这次不能匹配"，不能悄悄用空数据算出一堆无意义的分数。
 *
 * <p>对比 job / ai 两个 fallback：它们返回空集合或降级实现，因为那两个的缺失
 * 只降低结果质量，不影响正确性。<b>哪些依赖可降级、哪些不可降级，是业务决策，
 * 不是技术决策</b> —— 这个判断要写在代码注释里，否则半年后没人记得。
 */
@Component
public class UserFallback implements UserFeignClient {

    private static final Logger log = LoggerFactory.getLogger(UserFallback.class);

    @Override
    public ResumeView fetchLatestResume(Long userId) {
        log.warn("user-service unavailable, cannot fetch resume for userId={}", userId);
        return null;
    }
}
