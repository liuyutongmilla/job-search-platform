package com.jobsearch.jobservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;


@Schema(name = "Job", description = "职位信息")
public record JobDto(

        @Schema(description = "职位 ID，创建时不用传", accessMode = Schema.AccessMode.READ_ONLY)
        Long jobId,

        @NotBlank(message = "职位名称不能为空")
        @Size(max = 200, message = "职位名称最长 200 字")
        String title,

        @NotBlank(message = "公司名称不能为空")
        @Size(max = 200, message = "公司名称最长 200 字")
        String company,

        @NotBlank(message = "城市不能为空")
        @Size(max = 100, message = "城市名最长 100 字")
        String city,

        @Min(value = 0, message = "薪资下限不能为负")
        Integer minSalary,

        @Min(value = 0, message = "薪资上限不能为负")
        Integer maxSalary,

        @Min(value = 0, message = "工作年限要求不能为负")
        Integer requiredYears,

        @NotBlank(message = "职位描述不能为空")
        String description,

        @Schema(description = "OPEN / CLOSED", accessMode = Schema.AccessMode.READ_ONLY)
        String status,

        @NotEmpty(message = "至少填写一项技能要求")
        Set<String> skills,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime postedAt
) {
}
