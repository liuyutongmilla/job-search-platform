package com.jobsearch.matchingservice.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 全局拦截器：自动透传 correlation-id。
 *
 * <p>banking 项目是在<b>每个</b> Feign 方法签名里手写一个
 * {@code @RequestHeader("eazybank-correlation-id") String correlationId} 参数。
 * 那样做有两个问题：
 * <ol>
 *   <li>容易漏 —— 新加一个方法忘了带，链路就断了，而且不会报错</li>
 *   <li>污染接口 —— 业务接口的签名里混进了基础设施参数</li>
 * </ol>
 *
 * <p>用拦截器统一处理之后，Feign 接口只声明业务参数，
 * correlation-id 由框架无声地带上去。
 *
 * <p>ID 从哪来？OpenTelemetry javaagent 会把 trace 上下文放进 SLF4J 的 MDC，
 * 我们从那里读。如果没有 agent（本地裸跑），就退回读入站请求头 —— 见
 * {@link CorrelationIdFilter}。
 */
@Configuration
public class FeignConfig {

    public static final String CORRELATION_ID = "jobsearch-correlation-id";

    @Bean
    public RequestInterceptor correlationIdInterceptor() {
        return template -> {
            String correlationId = MDC.get(CORRELATION_ID);
            if (correlationId != null && !correlationId.isBlank()) {
                template.header(CORRELATION_ID, correlationId);
            }
        };
    }
}
