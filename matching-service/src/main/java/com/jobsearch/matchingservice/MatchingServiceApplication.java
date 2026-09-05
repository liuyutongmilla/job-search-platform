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

/**
 * 匹配编排服务 —— 本项目里最能体现"微服务韧性设计"的一个模块。
 *
 * <p>它自己几乎没有业务数据，工作是把三个上游拼起来：
 * <pre>
 *   user-service  取简历（结构化解析结果）
 *   job-service   取候选职位（结构化条件粗筛）
 *   ai-service    对候选职位精排打分
 * </pre>
 *
 * <p>三个上游<b>每一个</b>都可能挂，所以每一个都有 fallback。
 * 特别是 ai-service —— 它挂了不代表功能不可用，而是退化成规则打分（{@code RuleBasedScorer}）。
 * 用户仍然能看到推荐结果，只是解释质量下降。这就是"部分可用优于整体不可用"。
 */
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
