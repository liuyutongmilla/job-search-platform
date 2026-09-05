package com.jobsearch.notificationservice.functions;

import com.jobsearch.notificationservice.event.MatchComputedEvent;
import com.jobsearch.notificationservice.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 消费 topic {@code match-computed}。
 *
 * <p>邮件和短信是<b>两个独立的 Consumer</b>，各自绑定到同一个 topic 但用<b>不同的
 * 消费者组</b>（见 application.yml）。这样：
 * <ul>
 *   <li>邮件网关挂了，短信照发 —— 故障隔离</li>
 *   <li>各自独立重试、独立 DLQ、独立扩缩容</li>
 * </ul>
 *
 * <p>banking 项目用的是 {@code function.definition: email|sms} 函数组合
 * （email 的返回值喂给 sms）。那个写法能演示 Spring Cloud Function 的组合能力，
 * 但把两个渠道串成了一条链 —— 前一个失败后一个就不执行。生产上不该这么耦合。
 */
@Configuration
public class NotificationFunctions {

    private static final Logger log = LoggerFactory.getLogger(NotificationFunctions.class);

    @Bean
    public Consumer<MatchComputedEvent> emailNotification(NotificationSender sender) {
        return event -> {
            log.info("received match-computed for email: userId={} count={}",
                    event.userId(), event.matchCount());
            if (event.matchCount() == 0) {
                // 没有匹配就别发信打扰用户 —— 这是产品决策，写在代码里要注明
                log.debug("no matches for userId={}, skipping notification", event.userId());
                return;
            }
            // 不 try-catch：失败让 Spring Cloud Stream 重试，耗尽后进 DLQ。
            // 自己吞掉异常 = offset 被提交 = 消息永久丢失。
            sender.sendEmail(event);
        };
    }

    @Bean
    public Consumer<MatchComputedEvent> smsNotification(NotificationSender sender) {
        return event -> {
            if (event.matchCount() == 0) {
                return;
            }
            sender.sendSms(event);
        };
    }
}
