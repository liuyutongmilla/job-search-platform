package com.jobsearch.jobservice.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Job extends BaseEntity {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "company", nullable = false, length = 200)
    private String company;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    
    @Column(name = "min_salary")
    private Integer minSalary;

    
    @Column(name = "max_salary")
    private Integer maxSalary;

    
    @Column(name = "required_years")
    private Integer requiredYears;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", length = 100)
    private Set<String> skills = new LinkedHashSet<>();
}
