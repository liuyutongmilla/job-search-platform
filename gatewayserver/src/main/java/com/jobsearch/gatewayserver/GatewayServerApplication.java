package com.jobsearch.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 统一入口。三件事：路由、韧性策略、流量染色。
 *
 * <p>和 banking 项目最大的差别在 {@code .uri(...)}：
 * <pre>
 *   banking (K8s 服务发现)  →  .uri("http://accounts:8080")
 *   本项目  (Eureka)        →  .uri("lb://USER-SERVICE")
 * </pre>
 * {@code lb://} 前缀表示"交给 Spring Cloud LoadBalancer 按服务名去 Eureka 查实例
 * 并做客户端负载均衡"。服务名就是各服务的 {@code spring.application.name}（大小写不敏感）。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }

    @Bean
    public RouteLocator jobSearchRouteConfig(RouteLocatorBuilder builder) {
        return builder.routes()

                // 用户服务：简历上传是慢操作，给它单独的熔断器
                .route(p -> p
                        .path("/jobsearch/users/**")
                        .filters(f -> f
                                .rewritePath("/jobsearch/users/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport")))
                        .uri("lb://USER-SERVICE"))

                // 职位服务：读多写少，GET 幂等所以可以安全重试；再加限流保护数据库
                .route(p -> p
                        .path("/jobsearch/jobs/**")
                        .filters(f -> f
                                .rewritePath("/jobsearch/jobs/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .retry(r -> r
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())))
                        .uri("lb://JOB-SERVICE"))

                // 匹配服务：链路最长（要调 user + job + ai），超时窗口也最宽
                .route(p -> p
                        .path("/jobsearch/matching/**")
                        .filters(f -> f
                                .rewritePath("/jobsearch/matching/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(c -> c
                                        .setName("matchingServiceCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport")))
                        .uri("lb://MATCHING-SERVICE"))

                .build();
    }

    /**
     * 网关级熔断器的默认参数。
     *
     * <p>30 秒的超时是为匹配链路留的 —— AI 打分本来就慢。
     * 注意这个值必须大于下游服务自己的超时，否则网关先断，下游的重试和降级都白做了。
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(30))
                        .build())
                .build());
    }

    /**
     * 令牌桶：稳态 20 请求/秒，突发上限 40。
     *
     * <p>banking 项目那里写的是 {@code new RedisRateLimiter(1, 1, 1)}（1 请求/秒），
     * 那是为了课堂演示能立刻看到 429。真实业务别这么配。
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }

    /**
     * 限流维度。这里按登录用户 ID 限流；取不到就退化到按 IP。
     * 注意：{@code X-User-Id} 是客户端可伪造的，接了 Keycloak 之后应该从 JWT 的 sub 取。
     */
    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> Mono
                .justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                .switchIfEmpty(Mono.justOrEmpty(
                        exchange.getRequest().getRemoteAddress() == null
                                ? null
                                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()))
                .defaultIfEmpty("anonymous");
    }
}
