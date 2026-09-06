package com.jobsearch.matchingservice.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
