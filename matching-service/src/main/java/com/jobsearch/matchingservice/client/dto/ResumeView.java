package com.jobsearch.matchingservice.client.dto;

/**
 * user-service 返回体的<b>本地视图</b>。
 *
 * <p>注意这不是从 user-service 依赖过来的类，而是在这里重新声明了一份。
 * 这是有意的：
 * <ul>
 *   <li>如果依赖 user-service 的 jar，两个服务就产生了编译期耦合，无法独立发版</li>
 *   <li>本地视图只声明<b>本服务真正用到的字段</b> —— user-service 加字段不会影响这里，
 *       Jackson 默认会忽略未知字段</li>
 * </ul>
 *
 * <p>代价是契约变更（改字段名、删字段）不会在编译期报错，只会在运行时反序列化出 null。
 * 这就是为什么真实项目需要<b>契约测试</b>（Spring Cloud Contract / Pact）来守住这条边界 ——
 * 阶段 8 补。
 */
public record ResumeView(
        Long resumeId,
        Long userId,
        String parseStatus,
        Integer version,
        String parsedJson
) {
    /** 只有解析完成的简历才能用于匹配 */
    public boolean isUsable() {
        return "DONE".equals(parseStatus) && parsedJson != null && !parsedJson.isBlank();
    }
}
