package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ScoreRequest;
import com.jobsearch.matchingservice.client.dto.ScoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调 ai-service 做批量精排打分。
 *
 * <p>ai-service 还没实现（阶段 3）—— 但接口和 fallback 现在就定好了。
 * 好处是：<b>现在就能跑通整条链路</b>，AI 服务缺席时自动走 {@link AiFallback}
 * 的规则打分。这种"先定契约、再填实现"的顺序，能让你在最贵的部分（LLM）
 * 还没接上时，就把便宜的部分（编排、缓存、降级、落库）全部验证完。
 */
@FeignClient(name = "ai-service", fallback = AiFallback.class)
public interface AiFeignClient {

    @PostMapping("/api/ai/score")
    ScoreResponse score(@RequestBody ScoreRequest request);
}
