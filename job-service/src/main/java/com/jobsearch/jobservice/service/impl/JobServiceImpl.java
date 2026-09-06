package com.jobsearch.jobservice.service.impl;

import com.jobsearch.jobservice.dto.JobContactInfoDto;
import com.jobsearch.jobservice.dto.JobDto;
import com.jobsearch.jobservice.entity.Job;
import com.jobsearch.jobservice.entity.JobStatus;
import com.jobsearch.jobservice.event.JobPostedEvent;
import com.jobsearch.jobservice.exception.JobAlreadyExistsException;
import com.jobsearch.jobservice.exception.ResourceNotFoundException;
import com.jobsearch.jobservice.mapper.JobMapper;
import com.jobsearch.jobservice.repository.JobRepository;
import com.jobsearch.jobservice.service.IJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobServiceImpl implements IJobService {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    private final JobRepository jobRepository;
    private final StreamBridge streamBridge;
    private final JobContactInfoDto jobConfig;

    public JobServiceImpl(JobRepository jobRepository,
                          StreamBridge streamBridge,
                          JobContactInfoDto jobConfig) {
        this.jobRepository = jobRepository;
        this.streamBridge = streamBridge;
        this.jobConfig = jobConfig;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "jobSearch", allEntries = true)
    public JobDto createJob(JobDto jobDto) {
        jobRepository.findByTitleAndCompanyAndCity(jobDto.title(), jobDto.company(), jobDto.city())
                .ifPresent(existing -> {
                    throw new JobAlreadyExistsException(
                            "该公司在该城市已发布同名职位，jobId=" + existing.getJobId());
                });

        Job job = JobMapper.toNewEntity(jobDto);
        job.setPostedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);

        publishJobPosted(saved);
        return JobMapper.toDto(saved);
    }

    @Override
    @Cacheable(cacheNames = "jobs", key = "#jobId")
    public JobDto fetchJob(Long jobId) {
        log.debug("cache miss for jobId={}, hitting database", jobId);
        return JobMapper.toDto(requireJob(jobId));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"jobs", "jobSearch"}, allEntries = true)
    public boolean updateJob(Long jobId, JobDto jobDto) {
        Job job = requireJob(jobId);
        JobMapper.applyEditableFields(jobDto, job);
        jobRepository.save(job);
        
        publishJobPosted(job);
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"jobs", "jobSearch"}, allEntries = true)
    public boolean closeJob(Long jobId) {
        Job job = requireJob(jobId);
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
        return true;
    }

    @Override
    @Cacheable(
            cacheNames = "jobSearch",
            key = "T(java.util.Objects).hash(#city, #minSalary, #maxYears, #skill, #limit)")
    public List<JobDto> search(String city, Integer minSalary, Integer maxYears, String skill, Integer limit) {
        int max = jobConfig.search() == null || jobConfig.search().maxResults() == null
                ? 50
                : jobConfig.search().maxResults();
        int effective = (limit == null || limit <= 0) ? defaultPageSize() : Math.min(limit, max);

        Pageable pageable = PageRequest.of(0, effective);
        List<Job> jobs = jobRepository.search(
                JobStatus.OPEN, blankToNull(city), minSalary, maxYears, blankToNull(skill), pageable);

        log.debug("search city={} minSalary={} maxYears={} skill={} → {} hits",
                city, minSalary, maxYears, skill, jobs.size());
        return jobs.stream().map(JobMapper::toDto).toList();
    }

    private int defaultPageSize() {
        return jobConfig.search() == null || jobConfig.search().defaultPageSize() == null
                ? 20
                : jobConfig.search().defaultPageSize();
    }

    private Job requireJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", String.valueOf(jobId)));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    
    private void publishJobPosted(Job job) {
        JobPostedEvent event = new JobPostedEvent(
                job.getJobId(), job.getTitle(), job.getCity(), job.getSkills(), job.getPostedAt());
        boolean sent = streamBridge.send("jobPosted-out-0", event);
        log.info("published job-posted event jobId={} success={}", job.getJobId(), sent);
    }
}
