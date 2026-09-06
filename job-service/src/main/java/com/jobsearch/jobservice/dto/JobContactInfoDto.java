package com.jobsearch.jobservice.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


@ConfigurationProperties(prefix = "job")
public record JobContactInfoDto(
        Search search,
        Map<String, String> contactDetails
) {
    public record Search(Integer maxResults, Integer defaultPageSize) {
    }
}
