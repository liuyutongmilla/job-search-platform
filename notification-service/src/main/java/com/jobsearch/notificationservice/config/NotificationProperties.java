package com.jobsearch.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        Channels channels,
        Throttle throttle
) {
    public record Channels(Boolean email, Boolean sms) {
    }

    /**
     * 限流参数。批量重算时如果不限流，一个用户会在几分钟内收到几十封邮件 ——
     * 这是"技术上正确、体验上灾难"的典型。
     */
    public record Throttle(Integer windowMinutes, Integer maxPerWindow) {
    }

    public boolean emailEnabled() {
        return channels != null && Boolean.TRUE.equals(channels.email());
    }

    public boolean smsEnabled() {
        return channels != null && Boolean.TRUE.equals(channels.sms());
    }
}
