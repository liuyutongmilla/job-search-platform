package com.jobsearch.gatewayserver.filters;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * correlation-id 的读写工具。
 *
 * <p>这个 ID 是"业务流水号"：由网关生成，透传给所有下游服务，写进每一条日志。
 * 排障时拿它就能把一次用户请求在 7 个服务里留下的痕迹串起来。
 *
 * <p>别和 OpenTelemetry 自动生成的 {@code trace_id}/{@code span_id} 搞混：
 * 那两个是技术层面的链路追踪 ID（进 Tempo），这个是业务层面的（进日志和数据库）。
 * 生产环境一般会把两者统一成一个，避免维护两套。
 */
@Component
public class FilterUtility {

    public static final String CORRELATION_ID = "jobsearch-correlation-id";

    public String getCorrelationId(HttpHeaders requestHeaders) {
        List<String> values = requestHeaders.get(CORRELATION_ID);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate().header(name, value).build())
                .build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}
