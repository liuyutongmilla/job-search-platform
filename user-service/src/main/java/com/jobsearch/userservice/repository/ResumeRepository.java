package com.jobsearch.userservice.repository;

import com.jobsearch.userservice.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * 取用户最新的一份简历（版本号最大的那份）。历史版本保留，不删。
     *
     * <p>这个方法名是 Spring Data 的"派生查询"：方法名本身就是查询语义，
     * 不用写 SQL。{@code findFirstBy...OrderBy...Desc} 会被翻译成
     * {@code SELECT ... WHERE user_id = ? ORDER BY version DESC LIMIT 1}。
     */
    Optional<Resume> findFirstByUserIdOrderByVersionDesc(Long userId);

    /**
     * 当前最大版本号。
     *
     * <p>注意：这里<b>必须</b>写 {@code @Query} —— {@code findMaxVersionByUserId}
     * 不是 Spring Data 能识别的派生查询命名规则（没有 Max 这个关键字），
     * 光靠方法名会在启动时抛 {@code PropertyReferenceException}。
     * 这类错误只在应用启动时暴露，是"不写测试就发现不了"的典型。
     */
    @Query("SELECT MAX(r.version) FROM Resume r WHERE r.userId = :userId")
    Optional<Integer> findMaxVersion(@Param("userId") Long userId);
}
