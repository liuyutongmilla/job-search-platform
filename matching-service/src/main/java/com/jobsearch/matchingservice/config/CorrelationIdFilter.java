package com.jobsearch.matchingservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 把入站请求头里的 correlation-id 放进 MDC，让它能：
 * <ol>
 *   <li>自动出现在这个服务的每一条日志里（靠 logging.pattern 里的 %X{...}）</li>
 *   <li>被 {@link FeignConfig} 的拦截器读到并透传给下游</li>
 * </ol>
 *
 * <p><b>{@code finally} 里的 MDC.remove 不能省。</b> Tomcat 的线程是复用的，
 * 不清理的话下一个请求会继承上一个请求的 correlation-id ——
 * 日志会张冠李戴，排障时会把你带到完全错误的方向上。
 * 这类"线程局部变量泄漏"是 Web 应用最隐蔽的一类 bug。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(FeignConfig.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            // 直接打这个服务（绕过网关）时也要有 ID，否则链路断在这里
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(FeignConfig.CORRELATION_ID, correlationId);
        response.setHeader(FeignConfig.CORRELATION_ID, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(FeignConfig.CORRELATION_ID);
        }
    }
}
