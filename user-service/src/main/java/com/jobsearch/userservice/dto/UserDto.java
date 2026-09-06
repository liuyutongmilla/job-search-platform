package com.jobsearch.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "User", description = "用户资料")
public record UserDto(

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long userId,

        @NotBlank(message = "姓名不能为空")
        @Size(max = 100)
        String name,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 200)
        String email,

        
        
        @Pattern(regexp = "(^$|^1[3-9]\\d{9}$)", message = "手机号必须是 11 位有效号码")
        String phone,

        @Size(max = 100)
        String city,

        @Min(value = 0, message = "期望薪资不能为负")
        Integer expectedSalary
) {
}
