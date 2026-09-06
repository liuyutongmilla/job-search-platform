package com.jobsearch.notificationservice.functions;

import com.jobsearch.notificationservice.event.MatchComputedEvent;
import com.jobsearch.notificationservice.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;


@Configuration
public class NotificationFunctions {

    private static final Logger log = LoggerFactory.getLogger(NotificationFunctions.class);

    @Bean
    public Consumer<MatchComputedEvent> emailNotification(NotificationSender sender) {
        return event -> {
            log.info("received match-computed for email: userId={} count={}",
                    event.userId(), event.matchCount());
            if (event.matchCount() == 0) {
                
                log.debug("no matches for userId={}, skipping notification", event.userId());
                return;
            }
            
            
            sender.sendEmail(event);
        };
    }

    @Bean
    public Consumer<MatchComputedEvent> smsNotification(NotificationSender sender) {
        return event -> {
            if (event.matchCount() == 0) {
                return;
            }
            sender.sendSms(event);
        };
    }
}
