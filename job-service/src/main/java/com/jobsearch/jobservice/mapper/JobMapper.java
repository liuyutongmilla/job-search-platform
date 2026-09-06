package com.jobsearch.jobservice.mapper;

import com.jobsearch.jobservice.dto.JobDto;
import com.jobsearch.jobservice.entity.Job;
import com.jobsearch.jobservice.entity.JobStatus;

import java.util.LinkedHashSet;
import java.util.Set;


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

    
    public static Job toNewEntity(JobDto dto) {
        Job job = new Job();
        applyEditableFields(dto, job);
        job.setStatus(JobStatus.OPEN);
        return job;
    }

    
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
