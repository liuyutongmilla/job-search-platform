package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.ResumeView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调 user-service 取简历。
 *
 * <p><b>和 banking 项目最关键的一处差别</b>：
 * <pre>
 *   banking:  @FeignClient(name = "cards", url = "http://cards:9000", fallback = ...)
 *   本项目:    @FeignClient(name = "user-service", fallback = ...)
 * </pre>
 * 去掉 {@code url} 之后，Feign 会拿 {@code name} 当服务名去 Eureka 查所有健康实例，
 * 交给 Spring Cloud LoadBalancer 轮询 —— 这才是客户端负载均衡。
 * 写死 url 的话，负载均衡完全依赖 K8s Service，Eureka 就白装了。
 *
 * <p>correlation-id 不在方法签名里 —— 由 {@link com.jobsearch.matchingservice.config.FeignConfig}
 * 的拦截器自动透传。banking 项目是每个方法手写一个 {@code @RequestHeader} 参数，
 * 容易漏，而且污染接口签名。
 */
@FeignClient(name = "user-service", fallback = UserFallback.class)
public interface UserFeignClient {

    @GetMapping("/api/{userId}/resume")
    ResumeView fetchLatestResume(@PathVariable("userId") Long userId);
}
