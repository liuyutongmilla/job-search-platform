package com.jobsearch.userservice.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditAwareImpl")
public class AuditAwareImpl implements AuditorAware<String> {

    private static final String SERVICE_NAME = "USER_SERVICE";

    @Override
    public Optional<String> getCurrentAuditor() {
        // TODO(接 Keycloak 后)：改为从 SecurityContextHolder 取真实操作员
        return Optional.of(SERVICE_NAME);
    }
}
