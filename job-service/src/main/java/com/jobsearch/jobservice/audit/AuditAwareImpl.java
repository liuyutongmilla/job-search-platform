package com.jobsearch.jobservice.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 提供 {@code created_by} / {@code updated_by} 的值。
 *
 * <p>banking 项目里这里硬编码返回 {@code "ACCOUNTS_MS"}，也就是"哪个服务写的"。
 * 那是最低标准。<b>生产环境应该记录真实操作员</b>：
 *
 * <pre>
 *   return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
 *           .filter(Authentication::isAuthenticated)
 *           .map(Authentication::getName)
 *           .or(() -&gt; Optional.of(SERVICE_NAME));   // 系统触发的操作退回服务名
 * </pre>
 *
 * <p>接 Keycloak 之后（阶段 7 前）把上面这段替换进来。审计字段记录不到人，
 * 就等于没有审计。
 */
@Component("auditAwareImpl")
public class AuditAwareImpl implements AuditorAware<String> {

    private static final String SERVICE_NAME = "JOB_SERVICE";

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SERVICE_NAME);
    }
}
