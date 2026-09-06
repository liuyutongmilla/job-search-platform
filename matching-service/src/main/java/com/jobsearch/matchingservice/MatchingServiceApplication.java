package com.jobsearch.matchingservice;

import com.jobsearch.matchingservice.config.MatchingProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableConfigurationProperties(MatchingProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "Matching Service REST API",
        description = "简历-职位匹配编排",
        version = "v1"))
public class MatchingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingServiceApplication.class, args);
    }
}
