package com.jobsearch.matchingservice.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditAwareImpl")
public class AuditAwareImpl implements AuditorAware<String> {

    private static final String SERVICE_NAME = "MATCHING_SERVICE";

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SERVICE_NAME);
    }
}
