package com.jobsearch.notificationservice;

import com.jobsearch.notificationservice.config.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 通知服务 —— 纯 Kafka 消费者，没有对外的业务 HTTP 接口。
 *
 * <p>对照 banking 项目的 {@code message} 服务：那个用了函数组合
 * {@code function.definition: email|sms}（email 的输出喂给 sms）。
 * 这里改成两个独立的 Consumer，原因是邮件和短信应该<b>互不影响</b> ——
 * 组合写法下邮件挂了短信也发不出去，这不是我们想要的。
 *
 * <p>这类"课程里为了演示某个特性而写的结构，到生产环境要改回朴素做法"的地方，
 * 是抄开源教学项目时最需要留意的。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
