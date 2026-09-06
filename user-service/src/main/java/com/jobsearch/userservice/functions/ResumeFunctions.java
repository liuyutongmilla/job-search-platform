package com.jobsearch.userservice.functions;

import com.jobsearch.userservice.event.ResumeParsedEvent;
import com.jobsearch.userservice.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;


@Configuration
public class ResumeFunctions {

    private static final Logger log = LoggerFactory.getLogger(ResumeFunctions.class);

    
    @Bean
    public Consumer<ResumeParsedEvent> resumeParsed(IUserService userService) {
        return event -> {
            log.info("received resume-parsed resumeId={} success={}", event.resumeId(), event.success());
            userService.applyParseResult(
                    event.resumeId(), event.success(), event.parsedJson(), event.errorMessage());
        };
    }
}
