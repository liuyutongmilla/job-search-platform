package com.jobsearch.jobservice.entity;

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
 * 审计基类 —— 每个 Entity 都继承它。
 *
 * <p>配合 {@code @EnableJpaAuditing} + {@link com.jobsearch.jobservice.audit.AuditAwareImpl}，
 * 这四个字段会被自动填充，业务代码里<b>一行都不用写</b>。
 *
 * <p>这是从 banking 项目直接搬过来的，也是它最值得抄的一段：
 * 「谁在什么时候创建/修改了这条数据」是监管审计的最低要求，
 * 而这里用零业务侵入的方式实现了。
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
