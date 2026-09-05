package com.jobsearch.jobservice.mapper;

import com.jobsearch.jobservice.dto.JobDto;
import com.jobsearch.jobservice.entity.Job;
import com.jobsearch.jobservice.entity.JobStatus;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * banking 项目的测试只有一个空的 {@code contextLoads()} —— 那等于没有测试。
 * 这里从最容易测、最容易出错的地方开始：纯函数的 Mapper。
 *
 * <p>先写这种不需要 Spring 上下文的单元测试，跑得快（毫秒级），
 * 能在每次 commit 前无痛执行。集成测试（@SpringBootTest + Testcontainers）阶段 8 再补。
 */
class JobMapperTest {

    @Test
    void newEntityIsAlwaysOpenAndIgnoresClientSuppliedStatus() {
        // 客户端恶意传 CLOSED 和一个假的 jobId，都应该被忽略
        JobDto dto = new JobDto(999L, "Java 后端", "Acme", "上海",
                20000, 30000, 3, "描述", "CLOSED", Set.of("Java"), null);

        Job job = JobMapper.toNewEntity(dto);

        assertThat(job.getJobId()).isNull();
        assertThat(job.getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    void skillsAreTrimmedAndBlanksDropped() {
        Set<String> messy = new LinkedHashSet<>();
        messy.add("  Java  ");
        messy.add("");
        messy.add("   ");
        messy.add("Kafka");

        JobDto dto = new JobDto(null, "T", "C", "上海",
                null, null, null, "d", null, messy, null);

        Job job = JobMapper.toNewEntity(dto);

        assertThat(job.getSkills()).containsExactly("Java", "Kafka");
    }

    @Test
    void nullSkillsBecomeEmptySetNotNpe() {
        JobDto dto = new JobDto(null, "T", "C", "上海",
                null, null, null, "d", null, null, null);

        Job job = JobMapper.toNewEntity(dto);

        assertThat(job.getSkills()).isEmpty();
    }
}
