package com.jobsearch.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 类名叫 {@code AppUser} 而不是 {@code User}：{@code user} 在 PostgreSQL 里是保留字，
 * 表名也因此叫 {@code app_users}。这种"数据库保留字"的坑，早撞一次比上线撞好。
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 200, unique = true)
    private String email;

    /** 个人敏感信息。真实项目里应该加密存储 —— 见 V1 migration 里的备注。 */
    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "city", length = 100)
    private String city;

    /** 期望月薪下限，用于匹配时的硬条件过滤 */
    @Column(name = "expected_salary")
    private Integer expectedSalary;
}
