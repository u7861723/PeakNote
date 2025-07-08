package com.peaknote.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {
    @Bean("transcriptTaskExecutor")
    public ThreadPoolTaskExecutor transcriptTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("transcript-pool-");

        executor.setKeepAliveSeconds(60); // 设置空闲销毁时间（非核心线程）
        // executor.setAllowCoreThreadTimeOut(true); // 可选，如果希望核心线程也回收

        executor.initialize();
        return executor;
    }
    
}
