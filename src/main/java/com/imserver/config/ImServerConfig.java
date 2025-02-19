package com.imserver.config;

import com.imserver.util.DelayedTaskManager;
import com.imserver.util.ThreadUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置
 */
@Configuration(proxyBeanMethods = false)
public class ImServerConfig {
    @Bean
    public ThreadUtil threadUtil() {
        return ThreadUtil.create(Runtime.getRuntime().availableProcessors());
    }

    @Bean
    public DelayedTaskManager delayedTaskManager() {
        return new DelayedTaskManager(threadUtil());
    }
}
