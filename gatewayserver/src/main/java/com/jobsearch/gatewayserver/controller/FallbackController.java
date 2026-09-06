package com.jobsearch.gatewayserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;


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
