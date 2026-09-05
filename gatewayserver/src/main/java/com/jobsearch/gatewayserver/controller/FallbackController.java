package com.jobsearch.gatewayserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 网关熔断兜底。路由上的 {@code setFallbackUri("forward:/contactSupport")} 指向这里。
 *
 * <p>返回 503 而不是 200 —— 状态码要如实反映"服务不可用"，
 * 否则前端和监控都会以为一切正常。这是 banking 项目那版（返回纯文本 200）该改的地方。
 */
@RestController
public class FallbackController {

    @RequestMapping("/contactSupport")
    public Mono<ResponseEntity<Map<String, Object>>> contactSupport() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "errorCode", "SERVICE_UNAVAILABLE",
                        "errorMessage", "服务暂时不可用，请稍后重试。若持续出现请联系 ops@jobsearch.example.com",
                        "errorTime", LocalDateTime.now().toString())));
    }
}
