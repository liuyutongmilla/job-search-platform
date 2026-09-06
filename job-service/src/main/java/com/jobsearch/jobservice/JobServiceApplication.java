package com.jobsearch.jobservice;

import com.jobsearch.jobservice.dto.JobContactInfoDto;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
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
