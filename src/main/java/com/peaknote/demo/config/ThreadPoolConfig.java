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

        executor.setKeepAliveSeconds(60); // Set idle destruction time (non-core threads)
        // executor.setAllowCoreThreadTimeOut(true); // Optional, if you want core threads to be recycled too

        executor.initialize();
        return executor;
    }
    
}
