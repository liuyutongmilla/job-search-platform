package com.jobsearch.userservice.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 简历文件存储抽象。
 *
 * <p>抽成接口是因为本地开发用磁盘、生产用 S3/OSS/MinIO，
 * 而业务代码不该知道这个区别。这也是唯一值得抽象的地方 ——
 * 别为"以后可能换"而抽象，要为"现在就有两种实现"而抽象。
 */
public interface ResumeStorage {

    /**
     * @return 存储 key，写进 {@code resumes.file_key}，ai-service 用它取文件
     */
    String store(Long userId, MultipartFile file);

    /** ai-service 通过它读回文件内容（阶段 3 会用到） */
    byte[] read(String fileKey);
}
