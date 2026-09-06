package com.jobsearch.jobservice.service;

import com.jobsearch.jobservice.dto.JobDto;

import java.util.List;


public interface IJobService {

    JobDto createJob(JobDto jobDto);

    JobDto fetchJob(Long jobId);

    boolean updateJob(Long jobId, JobDto jobDto);

    
    boolean closeJob(Long jobId);

    List<JobDto> search(String city, Integer minSalary, Integer maxYears, String skill, Integer limit);
}
