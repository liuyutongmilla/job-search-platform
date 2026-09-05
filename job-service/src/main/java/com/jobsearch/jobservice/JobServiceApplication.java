package com.jobsearch.jobservice;

import com.jobsearch.jobservice.dto.JobContactInfoDto;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 职位服务 —— <b>本项目的参考实现</b>。
 *
 * <p>其他业务服务（user / matching / notification）都按这个结构写：
 * <pre>
 *   controller/   HTTP 入口，只做参数校验和响应封装，不写业务逻辑
 *   service/      业务逻辑（接口 + impl 分离，方便测试时 mock）
 *   repository/   数据访问
 *   entity/       数据库映射
 *   dto/          对外契约（和 entity 分开，避免数据库结构泄漏到 API）
 *   mapper/       entity ⇄ dto 转换
 *   exception/    自定义异常 + 全局处理器
 *   audit/        审计字段填充
 * </pre>
 *
 * <p>DTO 和 Entity 为什么必须分开：Entity 改字段名不应该导致 API 破坏性变更；
 * 反过来 API 想加个计算字段也不应该被迫改表。这一层转换的成本，换来的是两边独立演进。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableConfigurationProperties(JobContactInfoDto.class)
@OpenAPIDefinition(
        info = @Info(
                title = "Job Service REST API",
                description = "职位发布与检索",
                version = "v1",
                contact = @Contact(name = "Job Service Team", email = "job-team@jobsearch.example.com")))
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
