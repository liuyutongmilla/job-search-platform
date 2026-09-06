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
 * API gateway for the platform.
 * Upstream: frontend and external API clients.
 * Downstream: application services resolved through Kubernetes discovery.
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

                // Routes user operations to user-service with circuit-breaker protection.
                
                .route(p -> p
                        .path("/jobsearch/users/**")
                        .filters(f -> f
                                .rewritePath("/jobsearch/users/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport")))
                        .uri("lb://user-service"))

                // Routes job traffic to job-service with retry and Redis-backed rate limiting.
                
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
                        .uri("lb://job-service"))

                // Routes matching requests to matching-service, which orchestrates downstream scoring calls.
                
                .route(p -> p
                        .path("/jobsearch/matching/**")
                        .filters(f -> f
                                .rewritePath("/jobsearch/matching/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(c -> c
                                        .setName("matchingServiceCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport")))
                        .uri("lb://matching-service"))

                .build();
    }

    
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(30))
                        .build())
                .build());
    }

    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }

    
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
