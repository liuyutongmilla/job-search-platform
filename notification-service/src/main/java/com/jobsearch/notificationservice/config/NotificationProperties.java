package com.jobsearch.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        Channels channels,
        Throttle throttle
) {
    public record Channels(Boolean email, Boolean sms) {
    }

    
    public record Throttle(Integer windowMinutes, Integer maxPerWindow) {
    }

    public boolean emailEnabled() {
        return channels != null && Boolean.TRUE.equals(channels.email());
    }

    public boolean smsEnabled() {
        return channels != null && Boolean.TRUE.equals(channels.sms());
    }
}
