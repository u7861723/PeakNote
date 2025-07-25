package com.peaknote.demo.config;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Random;

@Configuration
@EnableCaching
public class CacheConfig {

    private final Random random = new Random();

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 默认 TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
                //.disableCachingNullValues(); // 如果想缓存 null 可以改 true

        // 给指定缓存加随机 TTL
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("transcriptCache",
                        config.entryTtl(Duration.ofMinutes(10).plusSeconds(random.nextInt(60))))
                .withCacheConfiguration("urlEventCache",
                        config.entryTtl(Duration.ofMinutes(10).plusSeconds(random.nextInt(60))))
                .build();
    }

    @Bean
    public SimpleKeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
}
