package com.jobsearch.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 和 job-service 的 BaseEntity 一字不差 —— <b>这个重复是有意的</b>。
 *
 * <p>微服务的原则是"宁可重复代码，不要共享可变依赖"。如果把它抽成一个
 * {@code common-lib} 打成 jar 给所有服务依赖，那么：
 * <ul>
 *   <li>改一个字段要同时发布 7 个服务 —— 独立部署的能力就没了</li>
 *   <li>服务之间产生了编译期耦合，版本升级会互相牵制</li>
 * </ul>
 *
 * <p>可以共享的东西只有<b>不可变的契约</b>：事件 DTO、Feign 接口定义。
 * 有业务逻辑或框架注解的基类，宁可复制。
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
public class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 64)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", insertable = false, length = 64)
    private String updatedBy;
}
