package com.jobsearch.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    
    @Column(name = "file_key", nullable = false, length = 500)
    private String fileKey;

    @Column(name = "original_filename", length = 300)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 20)
    private ParseStatus parseStatus;

    
    @Column(name = "parsed_json", columnDefinition = "jsonb")
    private String parsedJson;

    
    @Column(name = "parse_error", length = 1000)
    private String parseError;

    
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
