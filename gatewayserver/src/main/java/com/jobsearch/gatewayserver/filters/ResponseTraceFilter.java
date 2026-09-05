package com.jobsearch.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 出站：把 correlation-id 写回响应头，方便前端 / 客户报障时直接把它贴给你。
 *
 * <p>注意这里用 {@code chain.filter(exchange).then(...)} —— 响应头必须在下游处理完
 * 之后才能加，所以要挂在 {@code then} 里，不能写在过滤器开头。
 */
@Configuration
public class ResponseTraceFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTraceFilter.class);

    private final FilterUtility filterUtility;

    public ResponseTraceFilter(FilterUtility filterUtility) {
        this.filterUtility = filterUtility;
    }

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            String correlationId = filterUtility.getCorrelationId(exchange.getRequest().getHeaders());
            if (correlationId != null
                    && !exchange.getResponse().getHeaders().containsHeader(FilterUtility.CORRELATION_ID)) {
                logger.debug("adding correlation-id to outbound headers: {}", correlationId);
                exchange.getResponse().getHeaders().add(FilterUtility.CORRELATION_ID, correlationId);
            }
        }));
    }
}
