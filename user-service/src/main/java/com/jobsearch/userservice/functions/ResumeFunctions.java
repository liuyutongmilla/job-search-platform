package com.jobsearch.userservice.functions;

import com.jobsearch.userservice.event.ResumeParsedEvent;
import com.jobsearch.userservice.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Kafka 消费端。
 *
 * <p>Spring Cloud Stream 的函数式模型：声明一个 {@code Consumer<T>} 的 Bean，
 * 在 {@code application.yml} 里用 {@code spring.cloud.function.definition} 激活，
 * 并用 {@code <beanName>-in-0} 绑定到 topic。框架负责反序列化、offset 提交、重试。
 *
 * <p>对照 banking 项目的 {@code AccountsFunctions.updateCommunication} —— 一模一样的模式。
 */
@Configuration
public class ResumeFunctions {

    private static final Logger log = LoggerFactory.getLogger(ResumeFunctions.class);

    /**
     * 消费 topic {@code resume-parsed}，回写解析结果。
     *
     * <p>这里刻意<b>不</b>捕获异常：抛出去让 Spring Cloud Stream 的重试和
     * DLQ（死信队列）机制接管。自己 try-catch 吞掉的话，消息会被当成消费成功、
     * offset 提交，数据就永久丢了 —— 这是消息消费最常见的错误。
     *
     * <p>DLQ 配置见 application.yml 的 {@code enableDlq}。
     */
    @Bean
    public Consumer<ResumeParsedEvent> resumeParsed(IUserService userService) {
        return event -> {
            log.info("received resume-parsed resumeId={} success={}", event.resumeId(), event.success());
            userService.applyParseResult(
                    event.resumeId(), event.success(), event.parsedJson(), event.errorMessage());
        };
    }
}
