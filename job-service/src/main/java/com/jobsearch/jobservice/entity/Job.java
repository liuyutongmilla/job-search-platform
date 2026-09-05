package com.jobsearch.jobservice.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Job extends BaseEntity {

    /**
     * 主键交给数据库自增。
     *
     * <p>对比 banking 项目里 {@code 1000000000L + new Random().nextInt(900000000)} 那种
     * 随机生成账号的做法 —— 那有重复风险，而且靠数据库唯一约束报错来兜底，不可取。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "company", nullable = false, length = 200)
    private String company;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    /** 月薪下限（元） */
    @Column(name = "min_salary")
    private Integer minSalary;

    /** 月薪上限（元） */
    @Column(name = "max_salary")
    private Integer maxSalary;

    /** 要求的最低工作年限 */
    @Column(name = "required_years")
    private Integer requiredYears;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    /**
     * 技能标签。用 {@code @ElementCollection} 映射到独立的 {@code job_skills} 表，
     * 而不是塞成一个逗号分隔的字符串 —— 那样就没法用 SQL 按技能过滤和建索引了。
     *
     * <p>{@code FetchType.EAGER} 在这里是有意的：技能列表很小（通常 &lt; 10 个），
     * 且几乎每次查职位都要用到。如果用 LAZY，检索时会触发 N+1 查询。
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", length = 100)
    private Set<String> skills = new LinkedHashSet<>();
}
