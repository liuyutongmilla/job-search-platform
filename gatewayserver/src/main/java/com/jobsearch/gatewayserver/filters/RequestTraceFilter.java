package com.jobsearch.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Order(1)
@Component
public class RequestTraceFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestTraceFilter.class);

    private final FilterUtility filterUtility;

    public RequestTraceFilter(FilterUtility filterUtility) {
        this.filterUtility = filterUtility;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        String existing = filterUtility.getCorrelationId(requestHeaders);

        if (existing != null) {
            logger.debug("correlation-id found in request: {}", existing);
            return chain.filter(exchange);
        }

        String correlationId = UUID.randomUUID().toString();
        logger.debug("correlation-id generated at gateway: {}", correlationId);
        return chain.filter(filterUtility.setCorrelationId(exchange, correlationId));
    }
}
