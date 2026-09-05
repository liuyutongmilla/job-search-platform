package com.jobsearch.matchingservice.entity;

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
@Table(name = "job_matches")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JobMatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    /**
     * 简历版本号。
     *
     * <p>唯一约束是 (user_id, job_id, resume_version) —— 简历改了就重算，
     * 旧版本的分数保留但不再使用。这样"用户更新简历后分数没变"这个 bug
     * 从数据模型层面就不可能发生。
     */
    @Column(name = "resume_version", nullable = false)
    private Integer resumeVersion;

    @Column(name = "score", nullable = false)
    private Integer score;

    /** 优势项，JSONB 数组 */
    @Column(name = "strengths", columnDefinition = "jsonb")
    private String strengths;

    /** 差距项，JSONB 数组 */
    @Column(name = "gaps", columnDefinition = "jsonb")
    private String gaps;

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false, length = 20)
    private MatchEngine engine;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
