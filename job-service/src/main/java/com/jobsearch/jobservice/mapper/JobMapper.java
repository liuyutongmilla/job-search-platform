package com.jobsearch.jobservice.mapper;

import com.jobsearch.jobservice.dto.JobDto;
import com.jobsearch.jobservice.entity.Job;
import com.jobsearch.jobservice.entity.JobStatus;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entity ⇄ DTO 转换。
 *
 * <p>手写而不是用 MapStruct/BeanUtils：字段少的时候手写最清晰，
 * 而且能显式控制"哪些字段允许从外部传入"—— 比如 {@code status} 和 {@code postedAt}
 * 只允许服务端设置，客户端传了也会被忽略。这是一道很便宜的安全边界。
 */
public final class JobMapper {

    private JobMapper() {
    }

    public static JobDto toDto(Job job) {
        return new JobDto(
                job.getJobId(),
                job.getTitle(),
                job.getCompany(),
                job.getCity(),
                job.getMinSalary(),
                job.getMaxSalary(),
                job.getRequiredYears(),
                job.getDescription(),
                job.getStatus() == null ? null : job.getStatus().name(),
                job.getSkills(),
                job.getPostedAt());
    }

    /** 新建：status / postedAt / 审计字段由服务端和 JPA 负责，不从 DTO 取。 */
    public static Job toNewEntity(JobDto dto) {
        Job job = new Job();
        applyEditableFields(dto, job);
        job.setStatus(JobStatus.OPEN);
        return job;
    }

    /** 更新：只覆盖客户端有权修改的字段。 */
    public static void applyEditableFields(JobDto dto, Job job) {
        job.setTitle(dto.title());
        job.setCompany(dto.company());
        job.setCity(dto.city());
        job.setMinSalary(dto.minSalary());
        job.setMaxSalary(dto.maxSalary());
        job.setRequiredYears(dto.requiredYears());
        job.setDescription(dto.description());
        job.setSkills(normalizeSkills(dto.skills()));
    }

    /**
     * 技能标签规范化：去空白、去空串、统一去重。
     *
     * <p>不做大小写归一化 —— "Java" 和 "java" 在展示上有区别。
     * 大小写不敏感的匹配在查询时用 {@code LOWER()} 处理。
     */
    private static Set<String> normalizeSkills(Set<String> raw) {
        Set<String> result = new LinkedHashSet<>();
        if (raw == null) {
            return result;
        }
        for (String s : raw) {
            if (s == null) {
                continue;
            }
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
