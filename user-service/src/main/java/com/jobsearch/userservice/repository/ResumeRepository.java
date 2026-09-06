package com.jobsearch.userservice.repository;

import com.jobsearch.userservice.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    
    Optional<Resume> findFirstByUserIdOrderByVersionDesc(Long userId);

    
    @Query("SELECT MAX(r.version) FROM Resume r WHERE r.userId = :userId")
    Optional<Integer> findMaxVersion(@Param("userId") Long userId);
}
