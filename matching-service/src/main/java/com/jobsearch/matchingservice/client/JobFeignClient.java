package com.jobsearch.matchingservice.client;

import com.jobsearch.matchingservice.client.dto.JobView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "job-service", fallback = JobFallback.class)
public interface JobFeignClient {

    /**
     * 结构化粗筛 —— RAG 方案 A 的第一阶段。
     *
     * <p>先用城市 / 薪资 / 年限 / 技能这些<b>硬条件</b>在数据库里把候选集缩小，
     * 再把这几十条交给 AI 精排。招聘场景的硬条件本来就是结构化的，
     * 纯向量检索会把"薪资完全不匹配"的职位召回上来。
     */
    @GetMapping("/api/search")
    List<JobView> search(@RequestParam(value = "city", required = false) String city,
                         @RequestParam(value = "minSalary", required = false) Integer minSalary,
                         @RequestParam(value = "maxYears", required = false) Integer maxYears,
                         @RequestParam(value = "skill", required = false) String skill,
                         @RequestParam(value = "limit", required = false) Integer limit);
}
