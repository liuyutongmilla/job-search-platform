package com.jobsearch.matchingservice.repository;

import com.jobsearch.matchingservice.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    Optional<JobMatch> findByUserIdAndJobIdAndResumeVersion(Long userId, Long jobId, Integer resumeVersion);

    /** 取用户在当前简历版本下的匹配结果，按分数降序 */
    List<JobMatch> findByUserIdAndResumeVersionOrderByScoreDesc(Long userId, Integer resumeVersion);
}
