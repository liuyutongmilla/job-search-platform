package com.jobsearch.jobservice.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 从 config server 拉下来的业务配置，绑定到强类型对象。
 *
 * <p>对应 {@code configserver/src/main/resources/config/job-service.yml} 里的 {@code job.*}。
 * 改这些参数只需要改配置仓库并重启（或调 {@code /actuator/refresh}），不用重新构建发版。
 *
 * <p>注意：config server 连不上时（{@code optional:} 前缀允许启动），
 * 这些字段会是 null。所以 {@code application.yml} 里保留了一份兜底默认值，
 * 且业务代码里对 null 做了防御 —— <b>配置中心不该成为单点故障</b>。
 */
@ConfigurationProperties(prefix = "job")
public record JobContactInfoDto(
        Search search,
        Map<String, String> contactDetails
) {
    public record Search(Integer maxResults, Integer defaultPageSize) {
    }
}
