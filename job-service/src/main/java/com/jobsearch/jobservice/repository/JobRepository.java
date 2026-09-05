package com.jobsearch.jobservice.repository;

import com.jobsearch.jobservice.entity.Job;
import com.jobsearch.jobservice.entity.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByTitleAndCompanyAndCity(String title, String company, String city);

    /**
     * 多条件检索。所有过滤条件都可选（传 null 即忽略）。
     *
     * <p><b>这是我们讨论过的 RAG 方案 A 的第一阶段</b>：先用结构化条件（城市 / 薪资 / 年限 / 技能）
     * 在数据库里把候选集从几万条缩到几十条，再交给 AI 精排打分。
     * 招聘场景的硬条件本来就是结构化的，纯向量检索反而会把"薪资完全不匹配"的职位召回上来。
     *
     * <p>返回 {@code List} 而不是 {@code Page}：{@code DISTINCT} + {@code LEFT JOIN} 元素集合
     * 时 Spring Data 自动生成的 count 查询容易算错，用 List + Pageable 规避掉这个坑。
     */
    @Query("""
            SELECT DISTINCT j FROM Job j
            LEFT JOIN j.skills s
            WHERE j.status = :status
              AND (:city         IS NULL OR LOWER(j.city) = LOWER(:city))
              AND (:minSalary    IS NULL OR j.maxSalary     >= :minSalary)
              AND (:maxYears     IS NULL OR j.requiredYears <= :maxYears)
              AND (:skill        IS NULL OR LOWER(s) = LOWER(:skill))
            ORDER BY j.postedAt DESC
            """)
    List<Job> search(@Param("status") JobStatus status,
                     @Param("city") String city,
                     @Param("minSalary") Integer minSalary,
                     @Param("maxYears") Integer maxYears,
                     @Param("skill") String skill,
                     Pageable pageable);
}
