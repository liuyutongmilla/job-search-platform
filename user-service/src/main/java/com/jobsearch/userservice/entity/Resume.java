package com.jobsearch.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 对象存储里的 key（S3/OSS/MinIO）。原始文件不进数据库。 */
    @Column(name = "file_key", nullable = false, length = 500)
    private String fileKey;

    @Column(name = "original_filename", length = 300)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 20)
    private ParseStatus parseStatus;

    /**
     * ai-service 解析出的结构化结果（JSONB）。
     *
     * <p>用 JSONB 而不是拆成一堆表：简历的结构会随 prompt 迭代而变，
     * 早期用 JSONB 保留灵活性，等结构稳定了再考虑规范化。
     * 需要按字段查询时 PostgreSQL 的 JSONB 也支持建 GIN 索引。
     */
    @Column(name = "parsed_json", columnDefinition = "jsonb")
    private String parsedJson;

    /** 解析失败时的原因，给运维和用户看 */
    @Column(name = "parse_error", length = 1000)
    private String parseError;

    /**
     * 版本号。用户重新上传简历时递增。
     *
     * <p>为什么需要：匹配结果缓存的 key 里带了 resumeVersion，
     * 简历一变，旧的匹配分数就自动失效，不需要手动清缓存。
     */
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
