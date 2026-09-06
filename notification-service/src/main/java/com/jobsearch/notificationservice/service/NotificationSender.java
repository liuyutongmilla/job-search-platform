package com.jobsearch.notificationservice.service;

import com.jobsearch.notificationservice.config.NotificationProperties;
import com.jobsearch.notificationservice.event.MatchComputedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final NotificationProperties props;

    public NotificationSender(NotificationProperties props) {
        this.props = props;
    }

    public void sendEmail(MatchComputedEvent event) {
        if (!props.emailEnabled()) {
            log.debug("email channel disabled, skipping userId={}", event.userId());
            return;
        }
        
        
        
        
        
        log.info("[EMAIL] userId={} 有 {} 个新的职位匹配，最高分 {}（引擎：{}）",
                event.userId(), event.matchCount(), event.topScore(), event.engine());
    }

    public void sendSms(MatchComputedEvent event) {
        if (!props.smsEnabled()) {
            log.debug("sms channel disabled, skipping userId={}", event.userId());
            return;
        }
        log.info("[SMS] userId={} 有 {} 个新的职位匹配", event.userId(), event.matchCount());
    }
}
