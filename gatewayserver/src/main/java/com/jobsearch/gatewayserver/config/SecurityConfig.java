package com.jobsearch.gatewayserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关鉴权。
 *
 * <p><b>当前状态：全部放开，仅供本地开发。</b> 接 Keycloak 之前不要部到公网。
 *
 * <p>⚠️ banking 项目在这里踩了一个真实的坑，值得单独记一笔。它是这么写的：
 * <pre>
 *   exchanges.pathMatchers(HttpMethod.GET).permitAll()              // ← 第一条
 *            .pathMatchers("/eazybank/accounts/**").hasRole("ACCOUNTS")
 * </pre>
 * Spring Security 的规则是<b>自上而下、首次匹配即生效</b>，所以
 * {@code GET /eazybank/accounts/api/fetch?mobileNumber=xxx} 会命中第一条 {@code permitAll}，
 * 后面的角色检查根本不执行 —— 任何人不带 token 就能查任意手机号的客户信息。
 *
 * <p>本项目的简历数据是个人敏感信息，这个错误绝不能重犯。正确写法见下方注释块：
 * 具体路径规则写在前面，兜底规则（{@code anyExchange}）写在最后。
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http.authorizeExchange(exchanges -> exchanges
                // actuator 探针要让 K8s 能匿名访问，否则 liveness/readiness 会一直失败
                .pathMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                .pathMatchers("/contactSupport").permitAll()
                // TODO(阶段 7 之前必须改掉)：接 Keycloak 后换成下面注释里的规则
                .anyExchange().permitAll());

        /* ---- 接 Keycloak 之后启用这一段，并删掉上面的 anyExchange().permitAll() ----

        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                .pathMatchers("/contactSupport").permitAll()
                // 具体规则在前 —— 顺序就是语义
                .pathMatchers(HttpMethod.POST, "/jobsearch/jobs/**").hasRole("RECRUITER")
                .pathMatchers(HttpMethod.PUT,  "/jobsearch/jobs/**").hasRole("RECRUITER")
                .pathMatchers(HttpMethod.DELETE, "/jobsearch/jobs/**").hasRole("RECRUITER")
                .pathMatchers("/jobsearch/jobs/**").hasAnyRole("RECRUITER", "CANDIDATE")
                .pathMatchers("/jobsearch/users/**").hasRole("CANDIDATE")
                .pathMatchers("/jobsearch/matching/**").hasAnyRole("CANDIDATE", "RECRUITER")
                // 兜底拒绝在最后：新加的路径默认是"需要认证"，而不是默认放开
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())));

        ---- */

        // 网关是无状态的 JWT 校验，没有 session/cookie，所以 CSRF 保护无意义
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

    /* ---- 接 Keycloak 之后一起启用：把 realm_access.roles 转成 Spring 的 ROLE_xxx ----

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    ---- */
}
