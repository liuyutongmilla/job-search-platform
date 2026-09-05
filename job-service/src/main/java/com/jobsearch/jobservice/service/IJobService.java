package com.jobsearch.jobservice.service;

import com.jobsearch.jobservice.dto.JobDto;

import java.util.List;

/**
 * 接口和实现分离，不是为了"可能换实现"，而是为了：
 * 1) Controller 的单元测试可以 mock 掉整个业务层；
 * 2) 把"这个服务对外提供什么能力"这件事，用一个文件说清楚。
 */
public interface IJobService {

    JobDto createJob(JobDto jobDto);

    JobDto fetchJob(Long jobId);

    boolean updateJob(Long jobId, JobDto jobDto);

    /** 软删除：置为 CLOSED，不物理删除 —— 已有的匹配记录还引用着它。 */
    boolean closeJob(Long jobId);

    List<JobDto> search(String city, Integer minSalary, Integer maxYears, String skill, Integer limit);
}
