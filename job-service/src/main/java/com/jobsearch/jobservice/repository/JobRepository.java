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
